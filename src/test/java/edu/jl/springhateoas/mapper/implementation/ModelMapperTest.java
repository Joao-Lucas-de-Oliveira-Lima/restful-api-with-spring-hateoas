package edu.jl.springhateoas.mapper.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ModelMapper}
 */
class ModelMapperTest {

    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
    }

    @Test
    @DisplayName("Should successfully map an object to the destination type")
    void shouldConvertObject() {
        SourceObject source = new SourceObject("John Doe", 30);
        Class<DestinationObject> destinationType = DestinationObject.class;

        DestinationObject result = modelMapper.convertObject(source, destinationType);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should successfully map a list of objects to the destination type")
    void shouldConvertList() {
        List<SourceObject> sourceList = List.of(
                new SourceObject("John Doe", 30),
                new SourceObject("Jane Doe", 25)
        );
        Class<DestinationObject> destinationType = DestinationObject.class;

        List<DestinationObject> resultList = modelMapper.convertList(sourceList, destinationType);

        assertThat(resultList).hasSize(2);
        assertThat(resultList.get(0).getName()).isEqualTo("John Doe");
        assertThat(resultList.get(0).getAge()).isEqualTo(30);
        assertThat(resultList.get(1).getName()).isEqualTo("Jane Doe");
        assertThat(resultList.get(1).getAge()).isEqualTo(25);
    }

    @Test
    @DisplayName("Should successfully copy properties from source to destination object")
    void shouldCopyProperties() {
        SourceObject source = new SourceObject("John Doe", 30);
        DestinationObject destination = new DestinationObject();

        modelMapper.copyProperties(source, destination);

        assertThat(destination.getName()).isEqualTo("John Doe");
        assertThat(destination.getAge()).isEqualTo(30);
    }

    static class SourceObject {
        private String name;
        private int age;

        public SourceObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    static class DestinationObject {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
