package com.medreminder.dao;

import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.model.Medicine;
import com.medreminder.model.OTCMedicine;
import com.medreminder.model.PrescriptionMedicine;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data Access Object for CRUD and search operations on medicines.
 * Demonstrates JDBC, Polymorphism (mapping to PrescriptionMedicine / OTCMedicine
 * and overloaded searchMedicines methods), and Collections (ArrayList, HashMap).
 */
public class MedicineDAO {

    public Medicine addMedicine(Medicine medicine) throws DatabaseOperationException {
        String sql = "INSERT INTO medicines (user_id, medicine_name, medicine_type, dosage, frequency, "
                   + "start_date, end_date, doctor_name, purpose_category, notes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, medicine.getUserId());
            ps.setString(2, medicine.getMedicineName());
            ps.setString(3, medicine.getMedicineType());
            ps.setString(4, medicine.getDosage());
            ps.setString(5, medicine.getFrequency());
            ps.setDate(6, Date.valueOf(medicine.getStartDate()));
            ps.setDate(7, Date.valueOf(medicine.getEndDate()));

            if (medicine instanceof PrescriptionMedicine) {
                ps.setString(8, ((PrescriptionMedicine) medicine).getDoctorName());
                ps.setString(9, null);
            } else if (medicine instanceof OTCMedicine) {
                ps.setString(8, null);
                ps.setString(9, ((OTCMedicine) medicine).getPurposeCategory());
            } else {
                ps.setString(8, null);
                ps.setString(9, null);
            }
            ps.setString(10, medicine.getNotes());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    medicine.setId(keys.getInt(1));
                }
            }
            return medicine;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add medicine.", e);
        }
    }

    public void updateMedicine(Medicine medicine) throws DatabaseOperationException {
        String sql = "UPDATE medicines SET medicine_name = ?, medicine_type = ?, dosage = ?, frequency = ?, "
                   + "start_date = ?, end_date = ?, doctor_name = ?, purpose_category = ?, notes = ? "
                   + "WHERE medicine_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, medicine.getMedicineName());
            ps.setString(2, medicine.getMedicineType());
            ps.setString(3, medicine.getDosage());
            ps.setString(4, medicine.getFrequency());
            ps.setDate(5, Date.valueOf(medicine.getStartDate()));
            ps.setDate(6, Date.valueOf(medicine.getEndDate()));

            if (medicine instanceof PrescriptionMedicine) {
                ps.setString(7, ((PrescriptionMedicine) medicine).getDoctorName());
                ps.setString(8, null);
            } else if (medicine instanceof OTCMedicine) {
                ps.setString(7, null);
                ps.setString(8, ((OTCMedicine) medicine).getPurposeCategory());
            } else {
                ps.setString(7, null);
                ps.setString(8, null);
            }
            ps.setString(9, medicine.getNotes());
            ps.setInt(10, medicine.getId());
            ps.setInt(11, medicine.getUserId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update medicine.", e);
        }
    }

    public void deleteMedicine(int medicineId, int userId) throws DatabaseOperationException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete medicine.", e);
        }
    }

    public ArrayList<Medicine> getMedicinesByUser(int userId) throws DatabaseOperationException {
        ArrayList<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE user_id = ? ORDER BY start_date DESC, medicine_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMedicine(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load medicines.", e);
        }
    }

    /**
     * Returns a HashMap keyed by medicineId for O(1) lookups in reminders & history views.
     */
    public HashMap<Integer, Medicine> getMedicineMapByUser(int userId) throws DatabaseOperationException {
        HashMap<Integer, Medicine> map = new HashMap<>();
        for (Medicine med : getMedicinesByUser(userId)) {
            map.put(med.getId(), med);
        }
        return map;
    }

    /**
     * Compile-time polymorphism (Method Overloading #1): search by keyword only.
     */
    public ArrayList<Medicine> searchMedicines(int userId, String keyword) throws DatabaseOperationException {
        return searchMedicines(userId, keyword, "ALL");
    }

    /**
     * Compile-time polymorphism (Method Overloading #2): search by keyword and medicine type.
     */
    public ArrayList<Medicine> searchMedicines(int userId, String keyword, String medicineType)
            throws DatabaseOperationException {
        ArrayList<Medicine> list = new ArrayList<>();
        boolean filterType = medicineType != null && !medicineType.equalsIgnoreCase("ALL");

        String sql = "SELECT * FROM medicines WHERE user_id = ? AND LOWER(medicine_name) LIKE ?"
                   + (filterType ? " AND medicine_type = ?" : "")
                   + " ORDER BY medicine_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%");
            if (filterType) {
                ps.setString(3, medicineType);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMedicine(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to search medicines.", e);
        }
    }

    private Medicine mapRowToMedicine(ResultSet rs) throws SQLException {
        int id = rs.getInt("medicine_id");
        int userId = rs.getInt("user_id");
        String name = rs.getString("medicine_name");
        String type = rs.getString("medicine_type");
        String dosage = rs.getString("dosage");
        String frequency = rs.getString("frequency");
        Date start = rs.getDate("start_date");
        Date end = rs.getDate("end_date");
        String doctor = rs.getString("doctor_name");
        String category = rs.getString("purpose_category");
        String notes = rs.getString("notes");
        Timestamp ts = rs.getTimestamp("created_at");

        if ("PRESCRIPTION".equalsIgnoreCase(type)) {
            return new PrescriptionMedicine(
                    id, userId, name, dosage, frequency,
                    start.toLocalDate(), end.toLocalDate(),
                    doctor, notes, ts != null ? ts.toLocalDateTime() : null
            );
        }
        return new OTCMedicine(
                id, userId, name, dosage, frequency,
                start.toLocalDate(), end.toLocalDate(),
                category, notes, ts != null ? ts.toLocalDateTime() : null
        );
    }
}
