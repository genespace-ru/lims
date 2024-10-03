package ru.biosoft.lims;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestOperation extends TestBe5AppDBTest
{
    @Before
    public void setUp()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
    }

    @Test
    public void test()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "test");
        map.put("beginDate", "2017-12-20");
        map.put("values", "1");
        OperationResult second = executeOperation("testtable", "Test 1D", "TestOperation", "", map).getSecond();

        Assert.assertEquals(OperationStatus.FINISHED, second.getStatus());
        Assert.assertEquals("test message", second.getMessage());
    }

}
