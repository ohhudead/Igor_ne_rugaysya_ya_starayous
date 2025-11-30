package ohhudead.reservationsystem.exception;

public class CategoryDeleteException  extends RuntimeException {

    public CategoryDeleteException(Long categoryId) {
        super("Cannot delete category " +categoryId + "because it has products");
    }
}
