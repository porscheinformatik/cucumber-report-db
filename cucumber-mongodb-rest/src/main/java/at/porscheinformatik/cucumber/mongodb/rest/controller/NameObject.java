package at.porscheinformatik.cucumber.mongodb.rest.controller;

public class NameObject
{
    private String id;
    private String name;

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "{\"id\"=\"" + id + "\", \"name\"=\"" + name + "\"}";
    }
}
