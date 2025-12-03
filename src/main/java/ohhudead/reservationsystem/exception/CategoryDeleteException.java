package ohhudead.reservationsystem.exception;

import org.springframework.http.HttpStatus;

public class CategoryDeleteException  extends ApplicationException {

    public CategoryDeleteException(Long id) {
        super(
                HttpStatus.BAD_REQUEST,
                "Category with id=" + id + " cannot be deleted because it has products");
    }
}
