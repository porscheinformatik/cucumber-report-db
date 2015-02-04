package at.porscheinformatik.cucumber.mongodb.rest.controller;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class QueryControllerTest
{
    @Test(dataProvider = "computeNumberOfSkips")
    public void last_ten_with_list_nine(int cursorLength, int lastNrOfDocuments, int expectedNrOfSkips)
    {
        QueryController queryController = new QueryController();
        int actualNrOfSkips = queryController.skipToLast(cursorLength, lastNrOfDocuments);
        assertEquals(actualNrOfSkips, expectedNrOfSkips);
    }

    @DataProvider
    public Object[][] computeNumberOfSkips()
    {
        return new Object[][]{
               {9, 10, 0},
               {11, 10, 1},
               {30, 10, 20}
                };
    }

}