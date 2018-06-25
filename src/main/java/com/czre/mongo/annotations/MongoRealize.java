package com.czre.mongo.annotations;


import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoRealize {

    RealizeType.List List() default RealizeType.List.ArrayList;

    RealizeType.Map Map() default RealizeType.Map.HashMap;

    RealizeType.Set Set() default RealizeType.Set.HashSet;

    boolean _id() default (false);

}
