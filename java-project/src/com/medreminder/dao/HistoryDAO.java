package com.medreminder.dao;

import com.medreminder.exception.DatabaseOperationException;
import com.medreminder.model.MedicineHistory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data Access Object for dose tracking history and dashboard summary metrics.
 */
public class HistoryDAO {

    public MedicineHistory logDose(MedicineHistory history) throws DatabaseOperationException {
        String sql = "INSERT INTO medicine_history (medicine_id, reminder_id, user_id, dose_date, "
                   + "scheduled_time, taken_at, status, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, history.getMedicineId());
            if (history.getReminderId() != null) {
                ps.setInt(2, history.getReminderId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, history.getUserId());
            ps.setDate(4, Date.valueOf(history.getDoseDate()));
            ps.setTime(5, Time.valueOf(history.getScheduledTime()));
            if (history.getTakenAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(history.getTakenAt()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setString(7, history.getStatus());
            ps.setString(8, history.getRemarks());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    history.setId(keys.getInt(1));
                }
            }
            return history;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to log medicine dose.", e);
        }
    }

    public ArrayList<MedicineHistory> getHistoryByUser(int userId) throws DatabaseOperationException {
        ArrayList<MedicineHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, m.medicine_name, m.dosage "
                   + "FROM medicine_history h "
                   + "INNER JOIN medicines m ON h.medicine_id = m.medicine_id "
                   + "WHERE h.user_id = ? "
                   + "ORDER BY h.dose_date DESC, h.scheduled_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int reminderVal = rs.getInt("reminder_id");
                    Integer reminderId = rs.wasNull() ? null : reminderVal;
                    Timestamp takenTs = rs.getTimestamp("taken_at");
                    Timestamp createdTs = rs.getTimestamp("created_at");

                    MedicineHistory mh = new MedicineHistory(
                            rs.getInt("history_id"),
                            rs.getInt("medicine_id"),
                            reminderId,
                            rs.getInt("user_id"),
                            rs.getDate("dose_date").toLocalDate(),
                            rs.getTime("scheduled_time").toLocalTime(),
                            takenTs != null ? takenTs.toLocalDateTime() : null,
                            rs.getString("status"),
                            rs.getString("remarks"),
                            createdTs != null ? createdTs.toLocalDateTime() : null
                    );
                    mh.setMedicineName(rs.getString("medicine_name"));
                    mh.setDosage(rs.getString("dosage"));
                    list.add(mh);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load medicine history.", e);
        }
    }

    /**
     * Aggregates dashboard KPIs into a HashMap:
     * TOTAL_MEDICINES, ACTIVE_REMINDERS, TAKEN_COUNT, MISSED_COUNT.
     */
    public HashMap<String, Integer> getDashboardStats(int userId) throws DatabaseOperationException {
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL_MEDICINES", 0);
        stats.put("ACTIVE_REMINDERS", 0);
        stats.put("TAKEN_COUNT", 0);
        stats.put("MISSED_COUNT", 0);

        String medCountSql = "SELECT COUNT(*) FROM medicines WHERE user_id = ?";
        String remCountSql = "SELECT COUNT(*) FROM reminders WHERE user_id = ? AND is_active = TRUE";
        String histSql = "SELECT status, COUNT(*) AS cnt FROM medicine_history WHERE user_id = ? GROUP BY status";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(medCountSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("TOTAL_MEDICINES", rs.getInt(1));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(remCountSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.put("ACTIVE_REMINDERS", rs.getInt(1));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(histSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String status = rs.getString("status");
                        int count = rs.getInt("cnt");
                        if ("TAKEN".equalsIgnoreCase(status)) {
                            stats.put("TAKEN_COUNT", count);
                        } else if ("MISSED".equalsIgnoreCase(status)) {
                            stats.put("MISSED_COUNT", count);
                        }
                    }
                }
            }
            return stats;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to compute dashboard statistics.", e);
        }
    }
}
