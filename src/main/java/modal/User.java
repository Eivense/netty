package modal;


import lombok.Data;

@Data
public class User {


  private int id;
  private int age;

  public User(int id, int age) {
    this.id = id;
    this.age = age;
  }



}
