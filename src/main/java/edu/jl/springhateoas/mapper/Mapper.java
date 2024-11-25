package edu.jl.springhateoas.mapper;

import java.util.List;

public interface Mapper {
    <O, D> D convertObject(O source, Class<D> destination);
    <O, D> List<D> convertList(List<O> sourceList, Class<D> destination);
    <O, D> void copyProperties(O source, D destination);
}
