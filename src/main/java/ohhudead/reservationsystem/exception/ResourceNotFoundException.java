package ohhudead.reservationsystem.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String resource, Long id) {
        super(
                HttpStatus.NOT_FOUND,
                resource + "with id=" + id + " not found"
        );
    }
}
