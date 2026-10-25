package com.technocamp.efor;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PerformanceApiTest {

    private static final String VALID = """
            {"dateInfo":"2026-10-20","beginTime":"08:30","endTime":"17:30","timeout":1,
             "excuse":"Doktor Randevusu","excuseHours":2}
            """;

    @Autowired
    MockMvc mvc;

    @Test
    void rejectsAnonymousAndWrongPassword() throws Exception {
        mvc.perform(get("/api/performances")).andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(get("/api/auth/me").with(httpBasic("ayse", "yanlis"))).andExpect(status().isUnauthorized());
    }

    @Test
    void agentSeesOnlyOwnRecords() throws Exception {
        mvc.perform(get("/api/performances").with(httpBasic("ayse", "ayse123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].agentId", everyItem(is("glb90084448"))));
    }

    @Test
    void createUpdateDeleteRoundTrip() throws Exception {
        String body = mvc.perform(post("/api/performances").with(httpBasic("ayse", "ayse123"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.beginTime").value("08:30"))
                .andExpect(jsonPath("$.firstName").value("Ayşe"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.id");

        mvc.perform(put("/api/performances/" + id).with(httpBasic("ayse", "ayse123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("17:30", "18:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endTime").value("18:00"));

        // Başka bir temsilci bu kaydı değiştiremez/silemez
        mvc.perform(delete("/api/performances/" + id).with(httpBasic("mehmet", "mehmet123")))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/performances/" + id).with(httpBasic("ayse", "ayse123")))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/performances/" + id).with(httpBasic("ayse", "ayse123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatesBusinessRules() throws Exception {
        mvc.perform(post("/api/performances").with(httpBasic("ayse", "ayse123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("17:30", "08:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mesai bitiş saati başlangıç saatinden sonra olmalıdır"));

        mvc.perform(post("/api/performances").with(httpBasic("ayse", "ayse123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("\"Doktor Randevusu\"", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mazeret saati girildiyse mazeret seçilmelidir"));

        mvc.perform(post("/api/performances").with(httpBasic("ayse", "ayse123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.replace("\"timeout\":1", "\"timeout\":-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mola negatif olamaz"));
    }

    @Test
    void reportIsAdminOnlyAndFilterable() throws Exception {
        mvc.perform(get("/api/reports/performances").with(httpBasic("ayse", "ayse123")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/reports/performances").param("agentId", "glb12312312")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[*].agentId", everyItem(is("glb12312312"))));

        mvc.perform(get("/api/reports/performances").param("from", "2000-01-01").param("to", "2000-01-02")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(get("/api/reports/performances/export").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
