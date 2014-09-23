package at.porscheinformatik.cucumber.mongodb.rest.controller.dto;

public class TagDTO
{
    private String name;
    private Long line;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getLine()
    {
        return line;
    }

    public void setLine(Long line)
    {
        this.line = line;
    }

}