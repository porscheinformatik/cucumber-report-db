package at.porscheinformatik.cucumber.mongodb.rest.controller;

public class ValueObject 
{
	private String id;
	private Long value;
	
	public String getId() {
	  return id;
	}
	public Long getValue() {
	  return value;
	}
	
	public void setValue(Long value) {
	  this.value = value;
	}
	
	@Override
	public String toString() {
	  return "ValueObject [id=" + id + ", value=" + value + "]";
	}
}
