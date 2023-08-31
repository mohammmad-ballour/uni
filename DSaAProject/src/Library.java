import java.util.*;
import java.util.stream.Stream;
import java.io.*;
import java.nio.file.Files;

public class Library {

    int nbooks;  //number of books in library
    int nusers;  //number of users registered
    static TreeMap<Integer, Book> bookContainer = new TreeMap<>();
   
    File booksFile = new File("C:/Users/pc/Desktop/DSaA/DSaAProject/src/Books.txt");
    File usersFile = new File("C:/Users/pc/Desktop/DSaA/DSaAProject/src/users.txt");
    List<Book> booksAlreadyInFile;
    List<User> usersAlreadyRegisterd;
    
    public Library() throws IOException {
        booksAlreadyInFile = getBooksFromFile(booksFile);
        usersAlreadyRegisterd = getUsersFromFile(usersFile);
        for(Book b: booksAlreadyInFile) {
            bookContainer.put(b.getBook_id(), b);
        }
    }

    public static void main(String[] args) throws IOException {
        Library library = new Library();    //C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\images\\Lost and Founder.jpeg
       // Book newBook = takeAdminData();
       // library.insert(newBook);

       library.delete(777);
       // System.out.println(library.get(101));
       // System.out.println(library.search(101));
        System.out.println(library.search("abc"));


        
        for(Map.Entry entry : library.bookContainer.entrySet()) {
            Book b = (Book) entry.getValue();
            // System.out.println(b+"\n");
        }  
        

    }

    private static Book takeAdminData() {
        Scanner scanner = new Scanner(System.in); 
        System.out.print("Enter book_id: ");
        int id = scanner.nextInt();
        if(bookContainer.get(id) != null) {
            System.out.println("This id already exists");
            System.exit(-1);
        }
        scanner.nextLine();
        System.out.print("Enter book_title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book_author: ");
        String author = scanner.nextLine();
        System.out.print("Enter book_quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter book_price: ");
        String price = scanner.nextLine();
        System.out.print("Enter book_isbn: ");
        long isbn = scanner.nextLong();
        scanner.nextLine();
        System.out.print("Enter book_publisher: ");
        String publisher = scanner.nextLine();
        System.out.print("Enter book_totalPages: ");
        int total_pages = scanner.nextInt();
        System.out.print("Enter book_rating: ");
        float rating = scanner.nextFloat();
        scanner.nextLine();
        System.out.print("Enter book_publishedDate: ");
        String published_date = scanner.nextLine();
        System.out.print("Enter book_imgPath: ");
        String imgPath = scanner.nextLine();
        
        Book newBook = new Book(id, title, author, quantity, price, isbn, publisher, total_pages, rating, published_date, imgPath);
        return newBook;
    }

    protected List<User> getUsersFromFile(File file) {
        List<User> list = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            User user = new User();
            Stream<String> stream = Files.lines(file.toPath());
            int lines = (int) stream.count();
            for(int i = 0; i < lines/4; i++) {
                String fullName = scanner.nextLine();
                user.setFirstName(fullName.substring(0, fullName.indexOf(" ")));
                user.setLastName(fullName.substring(1+fullName.indexOf(" ")));
                user.setEmail(scanner.nextLine());
                user.setPassword(scanner.nextLine());
                user.setAge(Integer.parseInt(scanner.nextLine()));
                list.add(user);
                // System.out.println(user);
                user = new User();
            }
            scanner.close();    stream.close();   
        } catch (IOException e) {e.printStackTrace();}
        return list;
    }

    protected List<Book> getBooksFromFile(File file) {
        List<Book> list = new ArrayList<>();
        try (Scanner scanner = new Scanner(file).useDelimiter("[\\w]+ -> ")) {
            Book book = new Book();
            int numOfBooks = Integer.parseInt(scanner.nextLine());
   
            for(int i = 0; i < numOfBooks; i++) {
                book.setBook_id(Integer.parseInt(scanner.next().trim()));
                book.setTitle(scanner.next().trim());
                book.setAuthor(scanner.next().trim());
                book.setIsbn(Long.parseLong(scanner.next().trim()));
                book.setQuantity(Integer.parseInt(scanner.next().trim()));
                book.setPrice(scanner.next().trim());
                book.setPublisher(scanner.next().trim());
                book.setTotal_pages(Integer.parseInt(scanner.next().trim()));
                book.setRating(Float.parseFloat(scanner.next().trim()));
                book.setPublished_date(scanner.next().trim());
                book.setImagePath(scanner.next().trim());
                list.add(book);
                book = new Book();
            } 
   
        } catch (IOException e) {e.printStackTrace();}
        return list;
    }

    public void insert(Book book) throws IOException {
        bookContainer.put(book.getBook_id(), book);
        addInFile(book);
    }

    private void addInFile(Book book) {
        File file;
        FileWriter writer;
        Scanner scanner;
        File newFile = new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\books.txt");
        StringBuilder fileContent = new StringBuilder();
        try {
            file = new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\books.txt");
            scanner = new Scanner(file);

            fileContent.append((Integer.parseInt(scanner.nextLine()) + 1) + "" + System.lineSeparator()); //update total num of books
            while(scanner.hasNextLine()) {
                int i = 12;
                while(i != 0) {
                    String t = scanner.nextLine();
                    fileContent.append(t + System.lineSeparator());
                    //System.out.println(i+" : "+t); 
                    i--;
                }
            }
            scanner.close();
            writer = new FileWriter(newFile);
            writer.append(fileContent);
            writer.write("book_id -> "+book.getBook_id()+"\n");
            writer.write("title -> "+book.getTitle()+"\n");
            writer.write("author -> "+book.getAuthor()+"\n");
            writer.write("isbn -> "+book.getIsbn()+"\n");
            writer.write("quantity -> "+book.getQuantity()+"\n");
            writer.write("price -> "+book.getPrice()+"\n");
            writer.write("publisher -> "+book.getPublisher()+"\n");
            writer.write("total_pages -> "+book.getTotal_pages()+"\n");
            writer.write("rating -> "+book.getRating()+"\n");
            writer.write("published_date -> "+book.getPublished_date()+"\n");
            writer.write("imgPath -> "+book.getImagePath()+"\n");
            writer.write("\n");
            writer.flush(); 

        } catch (IOException e) {e.printStackTrace();}
    }


    public void delete(int id) {
        bookContainer.put(id, null);
        deleteFromFile(id);
    }

    private void deleteFromFile(int book_id) {
        File file;
        FileWriter writer;
        Scanner scanner;
        File newFile;
        StringBuilder fileContent = new StringBuilder();
        try {
            file = new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\books.txt");
            newFile = new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\books.txt");
            scanner = new Scanner(file);
            int numOfBooks = Integer.parseInt(scanner.nextLine());

            while(scanner.hasNextLine()) {
                String t = scanner.nextLine();
                int id = Integer.parseInt(t.substring(t.indexOf(">")+2));

                int i = 11;
                if(id == book_id) {
                    removeFromFile(newFile, t);
                    String str;
                    while(i != 0) {
                        str = scanner.nextLine();
                        //System.out.println("i: "+i+" str: "+str);
                        removeFromFile(newFile, str);
                        i--;
                    }
                    break;
                }
                fileContent.append(t + System.lineSeparator());
                while(i != 0) {
                    t = scanner.nextLine();
                    fileContent.append(t + System.lineSeparator());
                    i--;
                }
            
            }          
            scanner.close();
            writer = new FileWriter(newFile);
            fileContent.insert(0, (numOfBooks - 1)+ "" + System.lineSeparator());
            //System.out.println(fileContent);
            writer.append(fileContent);
            writer.flush();  
        } catch (IOException e) {e.printStackTrace();}
    }
    

    private void removeFromFile(File file, String lineToRemove) throws IOException {
        File newFile = new File(file.toPath().toString());
        Scanner scanner = new Scanner(file);
        FileWriter writer;
        StringBuilder fileContent = new StringBuilder(); 
        String currentLine;

        while(scanner.hasNextLine()) {
            currentLine = scanner.nextLine();
            if(currentLine.equals(lineToRemove)) continue;
            fileContent.append(currentLine + System.lineSeparator());
        }
        scanner.close(); 
        writer = new FileWriter(newFile);
        writer.append(fileContent);
        writer.flush();
    }

    public static boolean search(String title) throws IOException {
        File booksFile = new File("C:/Users/pc/Desktop/DSaA/DSaAProject/src/Books.txt");
        Scanner scanner = new Scanner(booksFile).useDelimiter(" -> ");
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.length() > 9) {     // 9 for title -> 
                if(line.substring(9).equalsIgnoreCase(title))
                    return true;
            }
        }
        return false;
    }

    public static boolean search(int id) {
        return bookContainer.get(id) != null;
    }

    public static Book get(int id) {
        return bookContainer.get(id);
    }


}
