package com.framework.db.service;

import com.pro.framework.api.model.IModel;
import lombok.Data;

@Data
public class TestUser implements IModel {
    Long id;
    String name;
}
