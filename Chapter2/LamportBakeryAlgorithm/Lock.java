package Chapter2.LamportBakeryAlgorithm;

public interface Lock {
  public void bakery(int n);
  public void lock();
  public void unlock();
}
