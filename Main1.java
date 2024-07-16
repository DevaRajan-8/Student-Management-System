package practices;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Student implements Serializable {
    private String name;
    private int rollNumber;
    private String grade;
    private String email;

    public Student(String name, int rollNumber, String grade, String email) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.grade = grade;
        this.email = email;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getRollNumber() { return rollNumber; }
    public void setRollNumber(int rollNumber) { this.rollNumber = rollNumber; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Name: " + name + ", Roll Number: " + rollNumber + ", Grade: " + grade + ", Email: " + email;
    }
}

class StudentManagementSystem {
    private List<Student> students;
    private static final String FILE_NAME = "students.dat";

    public StudentManagementSystem() {
        students = new ArrayList<>();
        loadStudents();
    }

    public void addStudent(Student student) {
        students.add(student);
        saveStudents();
    }

    public void removeStudent(int rollNumber) {
        students.removeIf(student -> student.getRollNumber() == rollNumber);
        saveStudents();
    }

    public Student searchStudent(int rollNumber) {
        return students.stream()
                .filter(student -> student.getRollNumber() == rollNumber)
                .findFirst()
                .orElse(null);
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public void updateStudent(Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getRollNumber() == updatedStudent.getRollNumber()) {
                students.set(i, updatedStudent);
                break;
            }
        }
        saveStudents();
    }

    private void loadStudents() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            students = (List<Student>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No existing student data found. Starting with an empty list.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveStudents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(students);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class StudentManagementGUI extends JFrame {
    private StudentManagementSystem sms;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, rollNumberField, gradeField, emailField;
    private JButton addButton, editButton, deleteButton, searchButton;

    public StudentManagementGUI() {
        sms = new StudentManagementSystem();
        setTitle("Student Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createComponents();
        createTable();
        addComponents();
        refreshTable();

        setVisible(true);
    }

    private void createComponents() {
        nameField = new JTextField(15);
        rollNumberField = new JTextField(10);
        gradeField = new JTextField(5);
        emailField = new JTextField(20);

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        searchButton = new JButton("Search");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editStudent();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStudent();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchStudent();
            }
        });
    }

    private void createTable() {
        tableModel = new DefaultTableModel(new Object[]{"Name", "Roll Number", "Grade", "Email"}, 0);
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void addComponents() {
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Roll Number:"));
        inputPanel.add(rollNumberField);
        inputPanel.add(new JLabel("Grade:"));
        inputPanel.add(gradeField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(addButton);
        inputPanel.add(editButton);

        JPanel actionPanel = new JPanel();
        actionPanel.add(deleteButton);
        actionPanel.add(searchButton);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(studentTable), BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student student : sms.getAllStudents()) {
            tableModel.addRow(new Object[]{
                    student.getName(),
                    student.getRollNumber(),
                    student.getGrade(),
                    student.getEmail()
            });
        }
    }

    private void addStudent() {
        try {
            String name = nameField.getText();
            int rollNumber = Integer.parseInt(rollNumberField.getText());
            String grade = gradeField.getText();
            String email = emailField.getText();

            if (name.isEmpty() || grade.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled.");
                return;
            }

            Student student = new Student(name, rollNumber, grade, email);
            sms.addStudent(student);
            refreshTable();
            clearFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid roll number.");
        }
    }

    private void editStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.");
            return;
        }

        try {
            int rollNumber = Integer.parseInt(tableModel.getValueAt(selectedRow, 1).toString());
            Student student = sms.searchStudent(rollNumber);

            if (student != null) {
                String name = nameField.getText();
                String grade = gradeField.getText();
                String email = emailField.getText();

                if (!name.isEmpty()) student.setName(name);
                if (!grade.isEmpty()) student.setGrade(grade);
                if (!email.isEmpty()) student.setEmail(email);

                sms.updateStudent(student);
                refreshTable();
                clearFields();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid roll number.");
        }
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.");
            return;
        }

        int rollNumber = Integer.parseInt(tableModel.getValueAt(selectedRow, 1).toString());
        sms.removeStudent(rollNumber);
        refreshTable();
    }

    private void searchStudent() {
        try {
            int rollNumber = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter roll number to search:"));
            Student student = sms.searchStudent(rollNumber);

            if (student != null) {
                JOptionPane.showMessageDialog(this, student.toString());
            } else {
                JOptionPane.showMessageDialog(this, "Student not found.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid roll number.");
        }
    }

    private void clearFields() {
        nameField.setText("");
        rollNumberField.setText("");
        gradeField.setText("");
        emailField.setText("");
    }
}

public class Main1 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StudentManagementGUI();
            }
        });
    }
}