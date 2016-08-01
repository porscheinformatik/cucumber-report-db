package at.porscheinformatik.cucumber.mongodb.rest.controller;

public class RightsObject
{
    private String id;
    private String rights;

    public String getId()
    {
        return id;
    }

    public String getRights()
    {
        return rights;
    }

    public void setRights(String rights)
    {
        this.rights = rights;
    }

    @Override
    public String toString()
    {
        return "{\"id\"=\"" + id + "\", \"rights\"=\"" + rights + "\"}";
    }
}
