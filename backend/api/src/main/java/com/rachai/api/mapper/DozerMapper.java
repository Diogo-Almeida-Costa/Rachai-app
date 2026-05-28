package com.rachai.api.mapper;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;

public class DozerMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static <D> D parseObject(Object origin, Class<D> destination) {
        if (origin == null) return null;
        return mapper.map(origin, destination);
    }

    public static <O, D> List<D> parseListObjects(List<O> origin, Class<D> destination) {
        List<D> destinationObjects = new ArrayList<>();
        if (origin == null) return destinationObjects;
        for (O o : origin) {
            destinationObjects.add(mapper.map(o, destination));
        }
        return destinationObjects;
    }

    public static void mergeObject(Object origin, Object destination) {
        if (origin == null || destination == null) return;
        mapper.map(origin, destination);
    }
}
