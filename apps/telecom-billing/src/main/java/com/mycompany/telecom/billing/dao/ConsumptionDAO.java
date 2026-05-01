package com.mycompany.telecom.billing.dao;

import com.mycompany.telecom.billing.model.ConsumptionView;
import com.mycompany.telecom.billing.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ali
 */
public class ConsumptionDAO {

    /**
     * Returns all active consumption rows for the given contract, joined with
     * service_package to get name, type and total quota.
     *
     * @return
     */
    public List<ConsumptionView> findByContractId(int contractId) throws SQLException {
        List<ConsumptionView> list = new ArrayList<>();
        String sql = "SELECT sp.name AS pkg_name,sp.type::TEXT AS pkg_type,sp.amount AS total_quota,cc.consumption AS consumed,cc.starting_date FROM contract_consumption cc JOIN service_package sp ON sp.id=cc.service_package_id WHERE cc.contract_id=? ORDER BY sp.type,sp.priority";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ConsumptionView cv = new ConsumptionView();
                    cv.setPackageName(rs.getString("pkg_name"));
                    cv.setServiceType(rs.getString("pkg_type"));
                    cv.setTotalQuota(rs.getBigDecimal("total_quota"));
                    cv.setConsumed(rs.getBigDecimal("consumed"));
                    Date sd = rs.getDate("starting_date");
                    if (sd != null) {
                        cv.setStartingDate(sd.toLocalDate());
                    }
                    list.add(cv);
                }
            }
        }
        return list;
    }
}
