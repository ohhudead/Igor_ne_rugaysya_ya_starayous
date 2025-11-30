package ohhudead.reservationsystem.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super("Resource " + resourceName + " not found: " + id);
    }
}
