import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ToString(includeFieldNames = true)
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class Book implements Comparable<Book> {
    
    int book_id;
    String title;
    String author;
    int quantity;
    String price;
    long isbn;
    String publisher;
    int total_pages;
    float rating;
    String published_date;
    String imagePath;

    @Override
    public int compareTo(Book o) {
        if(getQuantity() > o.getQuantity()) {
            return 1;
        } else if(getQuantity() == o.getQuantity()) {
            return 0;
        } else
            return -1;
    }

    
  
}