package org.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "announcements")
@Slf4j
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Integer id;

    @Column(name = "title")
    @NotNull
    @NotBlank
    @Size(max = 255)
    private final String title;

    @Column(name = "description")
    @NotNull
    @NotBlank
    @Size(max = 3000)
    private final String description;

    @Column(name = "_when")
    @PastOrPresent
    private final LocalDate date;

    @Column(name = "award")
    private final Double award;

    @Column(name = "an_type")
    @Enumerated(value = EnumType.STRING)
    @NotNull
    private final Type type;

    @Column(name = "user_id")
    @Positive
    private final int authorId;

    @JsonCreator
    public Announcement(
            @JsonProperty("id") Integer id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("award") Double award,
            @JsonProperty("type") Type type,
            @JsonProperty("authorId") int authorId
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.award = award;
        this.type = type;
        this.authorId = authorId;
    }

    public boolean announcementValid(boolean isCreate) {
        if (isCreate) {
            if (id != null) {
                log.error("Объявление невалидное");
                return false;
            }
        }
        if (date.isBefore( LocalDate.of(2022, 9, 1))) {
            log.error("Объявление невалидное");
            return false;
        }
        if (type.equals(Type.LOST)) {
            if (award < 1.0) {
                log.error("Объявление невалидное");
                return false;
            }
        }
        if (type.equals(Type.HAS_FOUND)) {
            if (award != null) {
                log.error("Объявление невалидное");
                return false;
            }
        }
        return true;
    }
}
