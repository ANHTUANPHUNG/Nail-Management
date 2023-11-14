package com.example.nail.service.combo.request;

import com.example.nail.service.request.SelectOptionRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Data
@NoArgsConstructor
public class ComboEditRequest implements Validator {
    private String id;
    private String name;

    private String price;

    private String description;
    private List<String> idProducts;

    private SelectOptionRequest poster;

    private List<SelectOptionRequest> images;
    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        ComboEditRequest comboEditRequest = (ComboEditRequest) target;

        String name = comboEditRequest.name;
        String description = comboEditRequest.description;
        String price = comboEditRequest.price;
        List<String> idProducts = comboEditRequest.idProducts;

        if (name.length() < 1) {
            errors.rejectValue("Name", "name.length", "Tên phải có ít nhất là 1 ký tự.");
        }

        if (description.length() < 1) {
            errors.rejectValue("Description", "description.length", "Miêu tả chỉ phải có ít nhất là 1 ký tự.");
        }
        if (idProducts == null || idProducts.isEmpty()) {
            errors.rejectValue("idProducts", "idProducts.nullOrEmpty", "Danh sách sản phẩm không được để trống.");
        }
        if (!isNumeric(price)) {
            errors.rejectValue("Price", "price.notNumeric", "Giá sản phẩm phải là một số.");
        } else {
            double priceValue = Double.parseDouble(price);
            if (priceValue < 10000 || priceValue > 1000000) {
                errors.rejectValue("Price", "price.range", "Giá sản phẩm phải lớn hơn 10000 và bé hơn 1000000.");
            }
        }
    }
}
