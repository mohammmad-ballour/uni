import java.io.*;
import java.net.URL;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class ItemController {
    
    @FXML
    private Label itemISBN;

    @FXML
    private ImageView itemImage;

    @FXML
    private Label itemPrice;

    @FXML
    private Label itemTitle;

    @FXML
    private Label itemQuantity;

    @FXML
    private Label itemAuthor;

    @FXML
    private Button buyButton;
    
    public void showBook(Book book) {
        Image image = new Image(book.getImagePath(), 125, 190, false, false);
        itemImage.setImage(image);
        itemTitle.setText(book.getTitle());
        itemAuthor.setText("by: "+book.getAuthor());
        itemQuantity.setText("quantity: "+book.getQuantity());
        itemISBN.setText("isbn: "+book.getIsbn());
        itemPrice.setText("price: "+book.getPrice());
        // other fields can be shown here, but not prefered.
    }

    @FXML
    private void buyButtonClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BooksDashBoard.fxml"));
        BorderPane root = loader.load();
        BooksDashBoardController controller = loader.getController();
        VBox parent = (VBox)((Node)event.getSource()).getParent();
        Label title = (Label)(parent.getChildren().get(0));

        Label quantity = (Label)(parent.getChildren().get(4));
        int quan = Integer.parseInt(quantity.getText().substring(quantity.getText().indexOf(" ")+1));
        quantity.setText("quantity: "+--quan);
        
        if(quantity.getText().equals("quantity: 0")) buyButton.setDisable(true);
        updateQuantInBooksFile(title.getText(), quan);
        controller.addToListView(title.getText());  //does NOT work !

    }

    private void updateQuantInBooksFile(String title, int quan) {
        File file;
        FileWriter writer;
        Scanner scanner;
        File newFile = new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\books.txt");
        StringBuilder fileContent = new StringBuilder();
        try {
            file = new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\books.txt");
            scanner = new Scanner(file);

            fileContent.append(scanner.nextLine() + System.lineSeparator());      //total number of books
            while(scanner.hasNextLine()) {

                fileContent.append(scanner.nextLine() + System.lineSeparator());   //book id
                String t = scanner.nextLine();
                fileContent.append(t + System.lineSeparator());     //title 
                String bookName = t.substring(t.indexOf(">") + 2);

                fileContent.append(scanner.nextLine() + System.lineSeparator());    //author 
                fileContent.append(scanner.nextLine() + System.lineSeparator());    //isbn
   
                t = scanner.nextLine();     //quantity
                if(bookName.equals(title)) {
                    fileContent.append(t.substring(0, t.indexOf(">") + 2) 
                    + String.valueOf(quan) + System.lineSeparator()); 
                } else
                    fileContent.append(t + System.lineSeparator());    
                
                int i = 7;
                while(i != 0) { //remaining info
                    t = scanner.nextLine();
                    fileContent.append(t + System.lineSeparator()); 
                    i--;
                }
            }
            scanner.close();
            writer = new FileWriter(newFile);
            writer.append(fileContent);
            writer.flush();

        } catch (IOException e) {e.printStackTrace();}
    
    }


}