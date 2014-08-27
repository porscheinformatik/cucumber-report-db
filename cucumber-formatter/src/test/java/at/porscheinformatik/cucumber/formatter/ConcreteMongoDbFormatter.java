package at.porscheinformatik.cucumber.formatter;

public class ConcreteMongoDbFormatter extends MongoDbFormatter
{
    @Override
    protected String getHost()
    {
        return "localhost";
    }

    @Override
    protected int getPort()
    {
        return 27019;
    }

    @Override
    protected String getDbName()
    {
        return "cucumberBddReport";
    }

    @Override
    protected String getCollection()
    {
        return "coll1";
    }
}