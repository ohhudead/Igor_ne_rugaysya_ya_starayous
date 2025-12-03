package ohhudead.reservationsystem.exception;

import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends ApplicationException {

    public CategoryAlreadyExistsException(String name) {
        super(
                HttpStatus.BAD_REQUEST,
                "Category with name '" + name + "' already exists"
        );
    }

}
