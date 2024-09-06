package ru.biosoft.lims.services;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.adapters.QRecParser;

import javax.inject.Inject;
import java.util.List;

public class TestRecords
{
    @Inject private DbService db;

    public List<QRec> recsForUser(String userName)
    {
        return db.list("SELECT t.* FROM testtable t " +
                "INNER JOIN users u ON t.name = u.user_name " +
                "WHERE u.user_name = ?", new QRecParser(), userName);
    }

    public String findNameById(int id)
    {
        return db.oneString("SELECT name FROM testtable WHERE id = ?", id);
    }

    public QRec findById(int id)
    {
        return db.select("SELECT * FROM testtable WHERE id = ?", new QRecParser(), id);
    }
}
