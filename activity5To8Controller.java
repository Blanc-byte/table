import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class activity5To8Controller {
    @FXML
    private TextField subCode, name, status, search;
    @FXML
    private TableView<Person> cell;
    private Person selectedPerson;
    private FilteredList<Person> filteredData;
    @FXML 
    private TableColumn c1, c2;
    // Create an ObservableList to hold your data
    ObservableList<Person> data = FXCollections.observableArrayList();

    final String DB_URL = "jdbc:mysql://localhost/schedulingsystemdatabase";
    final String USER = "root";
    final String PASS = "";

    Connection con = null;

    public Connection connect() {
        String url = "jdbc:mysql://localhost:3306/schedulingsystemdatabase";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error");
        }

        return con;
    }

    public void initialize() {
        cell.getColumns().remove(c1);
        cell.getColumns().remove(c2);
        cell.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        connect();
        
        // Create TableColumn instances
        TableColumn<Person, String> subCode1 = new TableColumn<>("Subject Code");
        TableColumn<Person, String> name1 = new TableColumn<>("Name");
        TableColumn<Person, String> status1 = new TableColumn<>("Status");
        TableColumn<Person, Person> actionsColumn = new TableColumn<>("Actions");

        subCode1.setCellValueFactory(cellData -> cellData.getValue().subCodeProperty());
        name1.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        status1.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        actionsColumn.setCellFactory(param -> new ButtonTableCell());

        cell.getColumns().addAll(subCode1, name1, status1, actionsColumn);

        // Set your data to the ObservableList
        loadDatabaseData();
        
        filteredData = new FilteredList<>(data, p -> true);

        // Bind the TableView to the filtered data
        cell.setItems(filteredData);

        // Add a listener to the search field's text property to trigger filtering
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue.toLowerCase();
            filteredData.setPredicate(person -> {
                if (query == null || query.isEmpty()) {
                    return true;
                }

                // You can modify this condition to include other fields you want to search
                return person.getSubCode().toLowerCase().contains(query)
                        || person.getName().toLowerCase().contains(query)
                        || person.getStatus().toLowerCase().contains(query);
            });
        });
    }
    
    
    
    
    
    // Load data from the database into the ObservableList
    private void loadDatabaseData() {
        try {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT subID, subCode, subName, subStatus FROM subject");

            while (resultSet.next()) {
                String subID = resultSet.getString("subID");
                String subCode = resultSet.getString("subCode");
                String name = resultSet.getString("subName");
                String status = resultSet.getString("subStatus");

                data.add(new Person(subID, subCode, name, status));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set the data to the TableView
        cell.setItems(data);
        
    }
    

    public class ButtonTableCell extends TableCell<Person, Person> {
        private final Button editButton = new Button("Edit");

        ButtonTableCell() {
            editButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Person person = getTableView().getItems().get(getIndex());
                    selectedPerson = person;
                    subCode.setText(selectedPerson.getSubCode());
                    name.setText(selectedPerson.getName());
                    status.setText(selectedPerson.getStatus());
                }
            });
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(editButton);
            }
        }
    }

    public void saveClicked(ActionEvent event) throws SQLException {
        int selectedIndex = cell.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Person person = cell.getItems().get(selectedIndex);

            // Update the data in the ObservableList
            person.setSubCode(subCode.getText());
            person.setName(name.getText());
            person.setStatus(status.getText());

            // Update the database
            updateDatabase(person);

            // Clear the input fields
            subCode.clear();
            name.clear();
            status.clear();
        }
    }
    private void updateDatabase(Person person) throws SQLException {
        Statement statement = con.createStatement();

        String sql = "UPDATE subject SET subCode = '" + person.getSubCode() +
                     "', subName = '" + person.getName() +
                     "', subStatus = '" + person.getStatus()+
                        "' WHERE subID = " + person.getSubID();

        statement.executeUpdate(sql);

    }

    public static class Person {
        private final StringProperty subID;
        private final StringProperty subCode;
        private final StringProperty name;
        private final StringProperty status;

        public Person(String subID,String subCode, String name, String status) {
            this.subID = new SimpleStringProperty(subID);
            this.subCode = new SimpleStringProperty(subCode);
            this.name = new SimpleStringProperty(name);
            this.status = new SimpleStringProperty(status);
        }

        public StringProperty subCodeProperty() {
            return subCode;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty statusProperty() {
            return status;
        }
        
        public String getSubID() {
            return subID.get();
        }

        public void setSubID(String subID) {
            this.subID.set(subID);
        }

        public String getSubCode() {
            return subCode.get();
        }

        public void setSubCode(String subCode) {
            this.subCode.set(subCode);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}
