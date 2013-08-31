package at.porscheinformatik.common.utils.silkplugin.dto;

import java.util.List;

public class StepDTO
{
    private StepResultDTO result;
    private List<EmbeddingDTO> embeddings;
    private String name;
    private String keyword;
    private Integer line;

    public StepResultDTO getResult()
    {
        return result;
    }

    public void setResult(StepResultDTO result)
    {
        this.result = result;
    }

    public List<EmbeddingDTO> getEmbeddings()
    {
        return embeddings;
    }

    public void setEmbeddings(List<EmbeddingDTO> embeddings)
    {
        this.embeddings = embeddings;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public Integer getLine()
    {
        return line;
    }

    public void setLine(Integer line)
    {
        this.line = line;
    }
}