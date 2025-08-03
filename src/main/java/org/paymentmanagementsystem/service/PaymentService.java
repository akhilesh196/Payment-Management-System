package org.paymentmanagementsystem.service;

import org.paymentmanagementsystem.dto.PaymentDTO;
import org.paymentmanagementsystem.exception.AuthorizationException;
import org.paymentmanagementsystem.exception.PaymentException;
import org.paymentmanagementsystem.model.Payment;
import org.paymentmanagementsystem.model.User;
import org.paymentmanagementsystem.repository.PaymentRepository;
import org.paymentmanagementsystem.repository.StatusRepository;
import org.paymentmanagementsystem.util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final StatusRepository statusRepository;
    private final AuditService auditService;
    private final ExecutorService executorService;

    public PaymentService() throws SQLException, IOException {
        this.paymentRepository = new PaymentRepository();
        this.statusRepository = new StatusRepository();
        this.auditService = new AuditService();
        this.executorService = Executors.newFixedThreadPool(5);
    }

    public CompletableFuture<Payment> createPayment(PaymentDTO paymentDTO, User currentUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate input
                if (!ValidationUtil.isValidAmount(paymentDTO.getAmount())) {
                    throw new PaymentException("Invalid amount");
                }

                if (!ValidationUtil.isNotEmpty(paymentDTO.getType())) {
                    throw new PaymentException("Payment type cannot be empty");
                }

                // Get pending status ID
                org.paymentmanagementsystem.model.Status pendingStatus = statusRepository.findByName("PENDING");
                if (pendingStatus == null) {
                    throw new PaymentException("Default status not found");
                }

                // Create payment
                Payment payment = new Payment(
                        paymentDTO.getAmount(),
                        paymentDTO.getType(),
                        paymentDTO.getDescription(),
                        paymentDTO.getCategoryId(),
                        pendingStatus.getStatusId(),
                        currentUser.getUserId()
                );

                Payment savedPayment = paymentRepository.save(payment);

                // Log audit trail asynchronously
                auditService.logPaymentCreation(savedPayment, currentUser);

                return savedPayment;

            } catch (Exception e) {
                throw new RuntimeException("Error creating payment", e);
            }
        }, executorService);
    }

    public List<Payment> getPaymentsByUser(User currentUser) throws PaymentException {
        try {
            String role = currentUser.getRole().getRoleName().toLowerCase();

            switch (role) {
                case "admin":
                case "finance_manager":
                    return paymentRepository.findAll();
                case "viewer":
                    return paymentRepository.findByUserId(currentUser.getUserId());
                default:
                    throw new AuthorizationException("Invalid role");
            }
        } catch (SQLException | AuthorizationException e) {
            throw new PaymentException("Error retrieving payments", e);
        }
    }

    public CompletableFuture<Void> approvePayment(int paymentId, User currentUser) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!hasApprovalPermission(currentUser)) {
                    throw new AuthorizationException("User does not have approval permission");
                }

                Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
                if (paymentOpt.isEmpty()) {
                    throw new PaymentException("Payment not found");
                }

                Payment payment = paymentOpt.get();

                // Get approved status
                org.paymentmanagementsystem.model.Status approvedStatus = statusRepository.findByName("APPROVED");
                if (approvedStatus == null) {
                    throw new PaymentException("Approved status not found");
                }

                // Update status
                paymentRepository.updateStatus(paymentId, approvedStatus.getStatusId());

                // Log audit trail
                auditService.logStatusChange(payment, currentUser, "APPROVED");

            } catch (Exception e) {
                throw new RuntimeException("Error approving payment", e);
            }
        }, executorService);
    }

    public CompletableFuture<Void> rejectPayment(int paymentId, User currentUser) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!hasApprovalPermission(currentUser)) {
                    throw new AuthorizationException("User does not have approval permission");
                }

                Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
                if (paymentOpt.isEmpty()) {
                    throw new PaymentException("Payment not found");
                }

                Payment payment = paymentOpt.get();

                // Get rejected status
                org.paymentmanagementsystem.model.Status rejectedStatus = statusRepository.findByName("REJECTED");
                if (rejectedStatus == null) {
                    throw new PaymentException("Rejected status not found");
                }

                // Update status
                paymentRepository.updateStatus(paymentId, rejectedStatus.getStatusId());

                // Log audit trail
                auditService.logStatusChange(payment, currentUser, "REJECTED");

            } catch (Exception e) {
                throw new RuntimeException("Error rejecting payment", e);
            }
        }, executorService);
    }

    public List<Payment> getPaymentsByStatus(String statusName, User currentUser) throws PaymentException {
        try {
            List<Payment> allPayments = getPaymentsByUser(currentUser);

            return allPayments.stream()
                    .filter(payment -> payment.getStatus().getStatusName().equalsIgnoreCase(statusName))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new PaymentException("Error filtering payments by status", e);
        }
    }

    public CompletableFuture<Void> deletePayment(int paymentId, User currentUser) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!hasDeletePermission(currentUser)) {
                    throw new AuthorizationException("User does not have delete permission");
                }

                Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
                if (paymentOpt.isEmpty()) {
                    throw new PaymentException("Payment not found");
                }

                Payment payment = paymentOpt.get();

                // Log audit trail before deletion
                auditService.logPaymentDeletion(payment, currentUser);

                // Delete payment
                paymentRepository.deleteById(paymentId);

            } catch (Exception e) {
                throw new RuntimeException("Error deleting payment", e);
            }
        }, executorService);
    }

    private boolean hasApprovalPermission(User user) {
        String role = user.getRole().getRoleName().toLowerCase();
        return role.equals("admin") || role.equals("finance_manager");
    }

    private boolean hasDeletePermission(User user) {
        String role = user.getRole().getRoleName().toLowerCase();
        return role.equals("admin");
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
