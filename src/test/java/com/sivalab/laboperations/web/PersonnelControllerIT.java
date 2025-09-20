package com.sivalab.laboperations.web;

import com.sivalab.laboperations.common.AbstractIntegrationTest;
import com.sivalab.laboperations.entity.Personnel;
import com.sivalab.laboperations.entity.User;
import com.sivalab.laboperations.repo.PersonnelRepository;
import com.sivalab.laboperations.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PersonnelControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PersonnelRepository personnelRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        personnelRepository.deleteAll();
        user = new User("testuser", "password", "test@example.com", Collections.emptySet());
        userRepository.save(user);
    }

    @Test
    void shouldCreatePersonnel() throws Exception {
        Personnel personnel = new Personnel(null, user, "PhD", Collections.singletonList("Training 1"), new Date(), "Pass");

        this.mockMvc.perform(post("/api/personnel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personnel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.qualifications", is("PhD")));
    }

    @Test
    void shouldGetAllPersonnel() throws Exception {
        Personnel personnel = new Personnel(null, user, "PhD", Collections.singletonList("Training 1"), new Date(), "Pass");
        personnelRepository.save(personnel);

        this.mockMvc.perform(get("/api/personnel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].qualifications", is("PhD")));
    }
}
