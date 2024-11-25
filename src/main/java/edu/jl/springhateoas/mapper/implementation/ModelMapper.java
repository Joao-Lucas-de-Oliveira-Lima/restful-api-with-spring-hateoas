package edu.jl.springhateoas.mapper.implementation;

import edu.jl.springhateoas.mapper.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelMapper implements Mapper {
    private final org.modelmapper.ModelMapper mapper = new org.modelmapper.ModelMapper();

    @Override
    public <O, D> D convertObject(O source, Class<D> destination) {
        return mapper.map(source, destination);
    }

    @Override
    public <O, D> List<D> convertList(List<O> sourceList, Class<D> destination) {
        return sourceList.stream().map(source -> mapper.map(source, destination)).toList();
    }

    @Override
    public <O, D> void copyProperties(O source, D destination) {
        mapper.map(source, destination);
    }

}
