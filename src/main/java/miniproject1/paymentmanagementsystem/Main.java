package miniproject1.paymentmanagementsystem;

import miniproject1.paymentmanagementsystem.config.DatabaseConfig;
import miniproject1.paymentmanagementsystem.controller.AdminController;
import miniproject1.paymentmanagementsystem.controller.AuthController;
import miniproject1.paymentmanagementsystem.controller.PaymentController;
import miniproject1.paymentmanagementsystem.model.User;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static AuthController authController;
    private static PaymentController paymentController;
    private static AdminController adminController;

    public static void main(String[] args) {
        try {
            System.out.println("=== PAYMENT MANAGEMENT SYSTEM ===");
            System.out.println("Initializing application...");

            // Initialize database
            DatabaseConfig.getInstance();
            System.out.println("Database initialized successfully!");

            initializeControllers();
            showWelcomeMessage();
            runApplication();
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void initializeControllers() throws Exception {
        System.out.println("Initializing controllers...");
        authController = new AuthController();
        paymentController = new PaymentController();
        adminController = new AdminController();
        System.out.println("Application ready!");
    }

    private static void showWelcomeMessage() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           WELCOME TO PAYMENT MANAGEMENT SYSTEM");
        System.out.println("=".repeat(60));
        System.out.println("Features:");
        System.out.println("• Role-based access control (Admin, Finance Manager, Viewer)");
        System.out.println("• Payment creation and management");
        System.out.println("• Payment approval/rejection workflow");
        System.out.println("• Audit trail for all operations");
        System.out.println("• Real-time payment status tracking");
        System.out.println("=".repeat(60));
        System.out.println("TIP: Use the default admin credentials to get started!");
        System.out.println("=".repeat(60));
    }

    private static void runApplication() {
        User currentUser = null;

        while (true) {
            try {
                if (currentUser == null) {
                    currentUser = handleAuthenticationMenu();
                } else {

                    boolean shouldLogout = handleMainMenu(currentUser);
                    if (shouldLogout) {
                        authController.handleLogout(currentUser);
                        currentUser = null;
                        System.out.println("You have been logged out successfully!");
                    }
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private static User handleAuthenticationMenu() {
        System.out.println("\n=== AUTHENTICATION ===");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.print("Choose option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    return authController.handleLogin();
                case 2:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return null;
    }

    private static boolean handleMainMenu(User currentUser) {
        String roleName = currentUser.getRole().getRoleName().toLowerCase();

        System.out.println("\n=== MAIN MENU ===");
        System.out.println("Logged in as: " + currentUser.getName() + " (" + roleName + ")");

        return switch (roleName) {
            case "admin" -> showAdminMenu(currentUser);
            case "finance_manager" -> showFinanceManagerMenu(currentUser);
            case "viewer" -> showViewerMenu(currentUser);
            default -> {
                System.out.println("Unknown role. Limited functionality available.");
                yield showViewerMenu(currentUser);
            }
        };
    }

    private static boolean showAdminMenu(User currentUser) {
        System.out.println("1. Create Payment");
        System.out.println("2. View Payments");
        System.out.println("3. Filter Payments by Status");
        System.out.println("4. Approve Payment");
        System.out.println("5. Reject Payment");
        System.out.println("6. Delete Payment");
        System.out.println("7. Register New User");
        System.out.println("8. View All Teams");
        System.out.println("9. View Team Members");
        System.out.println("10. Manage Salaries");
        System.out.println("11. Generate Monthly Salaries");
        System.out.println("12. Generate Reports");
        System.out.println("13. Logout");
        System.out.print("Choose option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    paymentController.handleCreatePayment(currentUser);
                    break;
                case 2:
                    paymentController.handleViewPayments(currentUser);
                    break;
                case 3:
                    paymentController.handleFilterPaymentsByStatus(currentUser);
                    break;
                case 4:
                    adminController.handleApprovePayment(currentUser);
                    break;
                case 5:
                    adminController.handleRejectPayment(currentUser);
                    break;
                case 6:
                    adminController.handleDeletePayment(currentUser);
                    break;
                case 7:
                    authController.handleRegistration();
                    break;
                case 8:
                    adminController.handleViewAllTeams(currentUser);
                    break;
                case 9:
                    adminController.handleViewTeamMembers(currentUser);
                    break;
                case 10:
                    adminController.handleManageSalaries(currentUser);
                    break;
                case 11:
                    adminController.handleGenerateMonthlySalaries(currentUser);
                    break;
                case 12:
                    adminController.handleGenerateReports(currentUser);
                    break;
                case 13:
                    return true; // User chose to logout
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return false;
    }

    private static boolean showFinanceManagerMenu(User currentUser) {
        System.out.println("1. Create Payment");
        System.out.println("2. View Payments");
        System.out.println("3. Filter Payments by Status");
        System.out.println("4. Approve Payment");
        System.out.println("5. Reject Payment");
        System.out.println("6. Create Team");
        System.out.println("7. Manage Team Members");
        System.out.println("8. View My Teams");
        System.out.println("9. Generate Reports");
        System.out.println("10. Logout");
        System.out.print("Choose option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    paymentController.handleCreatePayment(currentUser);
                    break;
                case 2:
                    paymentController.handleViewPayments(currentUser);
                    break;
                case 3:
                    paymentController.handleFilterPaymentsByStatus(currentUser);
                    break;
                case 4:
                    adminController.handleApprovePayment(currentUser);
                    break;
                case 5:
                    adminController.handleRejectPayment(currentUser);
                    break;
                case 6:
                    adminController.handleCreateTeam(currentUser);
                    break;
                case 7:
                    adminController.handleManageTeamMembers(currentUser);
                    break;
                case 8:
                    adminController.handleViewMyTeams(currentUser);
                    break;
                case 9:
                    adminController.handleGenerateReports(currentUser);
                    break;
                case 10:
                    return true;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return false;
    }

    private static boolean showViewerMenu(User currentUser) {
        System.out.println("1. View My Payments");
        System.out.println("2. Filter My Payments by Status");
        System.out.println("3. Logout");
        System.out.print("Choose option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    paymentController.handleViewPayments(currentUser);
                    break;
                case 2:
                    paymentController.handleFilterPaymentsByStatus(currentUser);
                    break;
                case 3:
                    return true;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return false;
    }
}