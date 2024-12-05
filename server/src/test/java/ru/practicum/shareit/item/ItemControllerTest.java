package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private final ItemDto itemDto = new ItemDto(1L, "my item", "item description",
            1L, true, null, null, null, null);
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void saveNewItem() throws Exception {
        when(itemService.saveItem(any())).thenReturn(itemDto);

        mvc.perform(post("/items").content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(itemDto)))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(any())).thenReturn(itemDto);

        mvc.perform(patch("/items/1").content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(itemDto)))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemDto);

        mvc.perform(get("/items/1").content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void getItemsByOwner() throws Exception {
        when(itemService.getUsersItems(anyInt(), anyInt(), anyLong())).thenReturn(List.of(itemDto));

        mvc.perform(get("/items").characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON).header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
    }

    @Test
    void getFilmBySearch() throws Exception {
        when(itemService.searchItems(any(), any(), any())).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search?text=a&from=0&size=4").characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
    }

    @Test
    void saveNewComment() throws Exception {
        final CommentDto commentDtoOut = new CommentDto(1L, "comment", "user", LocalDateTime.now());

        when(itemService.saveNewComment(any(), anyLong(), anyLong())).thenReturn(commentDtoOut);

        mvc.perform(post("/items/1/comment").content(mapper.writeValueAsString(commentDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(commentDtoOut)))
                .andExpect(jsonPath("$.id", is(commentDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDtoOut.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDtoOut.getAuthorName())));
    }
}
