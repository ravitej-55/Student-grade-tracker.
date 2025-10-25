/*
 StudentGradeTrackerApp.java
 Single-file console Java Student Grade Tracker
 - Uses ArrayLists for students and grades
 - Add students, add grades, remove students, show summary
 - Export / import CSV (file paths relative to working directory)
 
 To compile:
   javac StudentGradeTrackerApp.java
 To run:
   java StudentGradeTrackerApp
*/
import java.util.ArrayList;
import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.stream.DoubleStream;
import java.io.*;

/* Student class */
class Student {
    private String name;
    private ArrayList<Double> grades;

    public Student(String name) {
        this.name = name.trim();
        this.grades = new ArrayList<>();
    }

    public String getName() { return name; }

    public void addGrade(double g) {
        if (g < 0) throw new IllegalArgumentException("Grade cannot be negative");
        grades.add(g);
    }

    public void setGrades(ArrayList<Double> newGrades) {
        grades = new ArrayList<>(newGrades);
    }

    public ArrayList<Double> getGrades() {
        return new ArrayList<>(grades);
    }

    public double getAverage() {
        if (grades.isEmpty()) return Double.NaN;
        double sum = 0;
        for (double d : grades) sum += d;
        return sum / grades.size();
    }

    public double getHighest() {
        if (grades.isEmpty()) return Double.NaN;
        return grades.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
    }

    public double getLowest() {
        if (grades.isEmpty()) return Double.NaN;
        return grades.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
    }

    @Override
    public String toString() {
        if (grades.isEmpty()) {
            return String.format("%s: No grades", name);
        } else {
            return String.format("%s | Grades: %s | Avg: %.2f | High: %.2f | Low: %.2f",
                name, grades.toString(), getAverage(), getHighest(), getLowest());
        }
    }
}

/* GradeTracker manager */
class GradeTracker {
    private ArrayList<Student> students = new ArrayList<>();

    public void addStudent(String name) {
        if (findStudentByName(name) != null) {
            System.out.println("Student already exists. Use a different name or update existing.");
            return;
        }
        students.add(new Student(name));
    }

    public boolean removeStudent(String name) {
        Student s = findStudentByName(name);
        if (s != null) {
            students.remove(s);
            return true;
        }
        return false;
    }

    public Student findStudentByName(String name) {
        for (Student s : students) {
            if (s.getName().equalsIgnoreCase(name.trim())) return s;
        }
        return null;
    }

    public double overallAverage() {
        DoubleStream all = students.stream()
            .flatMapToDouble(s -> s.getGrades().stream().mapToDouble(Double::doubleValue));
        OptionalDouble od = all.average();
        return od.isPresent() ? od.getAsDouble() : Double.NaN;
    }

    public double overallHighest() {
        return students.stream()
            .flatMapToDouble(s -> s.getGrades().stream().mapToDouble(Double::doubleValue))
            .max()
            .orElse(Double.NaN);
    }

    public double overallLowest() {
        return students.stream()
            .flatMapToDouble(s -> s.getGrades().stream().mapToDouble(Double::doubleValue))
            .min()
            .orElse(Double.NaN);
    }

    public void printAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students in the tracker yet.");
            return;
        }
        students.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        for (Student s : students) {
            System.out.println(s.toString());
        }
    }

    // CSV format: name,grade1,grade2,...
    public boolean exportCSV(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Student s : students) {
                StringBuilder sb = new StringBuilder();
                sb.append(s.getName());
                for (Double g : s.getGrades()) {
                    sb.append(",").append(g);
                }
                pw.println(sb.toString());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error exporting CSV: " + e.getMessage());
            return false;
        }
    }

    public boolean importCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 1) continue;
                String name = parts[0].trim();
                Student s = findStudentByName(name);
                if (s == null) {
                    s = new Student(name);
                    students.add(s);
                }
                ArrayList<Double> grades = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    try {
                        double g = Double.parseDouble(parts[i].trim());
                        grades.add(g);
                    } catch (NumberFormatException ex) {
                        // skip invalid grade tokens
                    }
                }
                s.setGrades(grades);
                count++;
            }
            System.out.println("Imported " + count + " lines from CSV.");
            return true;
        } catch (IOException e) {
            System.out.println("Error importing CSV: " + e.getMessage());
            return false;
        }
    }

    public boolean hasStudents() {
        return !students.isEmpty();
    }
}

/* Main application */
public class StudentGradeTrackerApp {
    private static GradeTracker tracker = new GradeTracker();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Student Grade Tracker ===");
        boolean quit = false;
        while (!quit) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": handleAddStudent(); break;
                case "2": handleAddGrade(); break;
                case "3": handleRemoveStudent(); break;
                case "4": handleShowSummary(); break;
                case "5": handleExportCSV(); break;
                case "6": handleImportCSV(); break;
                case "7": handleListStudents(); break;
                case "0": quit = true; break;
                default: System.out.println("Invalid option. Try again."); break;
            }
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1) Add new student");
        System.out.println("2) Add grade to student");
        System.out.println("3) Remove student");
        System.out.println("4) Show summary report");
        System.out.println("5) Export to CSV");
        System.out.println("6) Import from CSV");
        System.out.println("7) List students");
        System.out.println("0) Exit");
        System.out.print("Choose: ");
    }

    private static void handleAddStudent() {
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        tracker.addStudent(name);
        System.out.println("Added student: " + name);
    }

    private static void handleAddGrade() {
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();
        Student s = tracker.findStudentByName(name);
        if (s == null) {
            System.out.println("Student not found. Add them first? (y/n)");
            String ans = sc.nextLine().trim().toLowerCase();
            if (ans.equals("y")) {
                tracker.addStudent(name);
                s = tracker.findStudentByName(name);
            } else return;
        }
        System.out.print("Enter grade (numeric): ");
        String gStr = sc.nextLine().trim();
        try {
            double g = Double.parseDouble(gStr);
            if (g < 0) {
                System.out.println("Grade must be >= 0");
                return;
            }
            s.addGrade(g);
            System.out.println("Added grade " + g + " to " + s.getName());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number.");
        }
    }

    private static void handleRemoveStudent() {
        System.out.print("Enter student name to remove: ");
        String name = sc.nextLine().trim();
        boolean ok = tracker.removeStudent(name);
        System.out.println(ok ? "Removed." : "Student not found.");
    }

    private static void handleShowSummary() {
        if (!tracker.hasStudents()) {
            System.out.println("No students.");
            return;
        }
        System.out.println("\n--- Student List ---");
        tracker.printAllStudents();
        double overallAvg = tracker.overallAverage();
        double overallHigh = tracker.overallHighest();
        double overallLow = tracker.overallLowest();
        System.out.println("\n--- Overall Statistics ---");
        System.out.println("Overall Average: " + (Double.isNaN(overallAvg) ? "N/A" : String.format("%.2f", overallAvg)));
        System.out.println("Overall Highest: " + (Double.isNaN(overallHigh) ? "N/A" : String.format("%.2f", overallHigh)));
        System.out.println("Overall Lowest : " + (Double.isNaN(overallLow) ? "N/A" : String.format("%.2f", overallLow)));
    }

    private static void handleExportCSV() {
        System.out.print("Enter filename to export (e.g., students.csv): ");
        String fn = sc.nextLine().trim();
        if (fn.isEmpty()) {
            System.out.println("Filename empty.");
            return;
        }
        if (tracker.exportCSV(fn)) System.out.println("Exported to " + fn);
    }

    private static void handleImportCSV() {
        System.out.print("Enter filename to import (e.g., students.csv): ");
        String fn = sc.nextLine().trim();
        if (fn.isEmpty()) {
            System.out.println("Filename empty.");
            return;
        }
        tracker.importCSV(fn);
    }

    private static void handleListStudents() {
        tracker.printAllStudents();
    }
          }
