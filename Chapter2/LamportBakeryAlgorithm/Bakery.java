package Chapter2.LamportBakeryAlgorithm;

public class Bakery implements Lock {
  public boolean[] flag;
  public int[] label;

  @Override
  public void bakery(int n) {
    // Implementation of the bakery method
  }

  // constructor
  public Bakery(int n) {
    flag = new boolean[n];
    label = new int[n];
    ThreadID.setMaxThreads(n);

    for(int i=0; i<n; i++) {
      flag[i] = false; label[i] = 0;
    }
  }

  public void lock() {
    int i = ThreadID.get();

    // 1. Critical Section에 진입하고 싶다는 플래그 표시
    flag[i] = true;

    // 2. doorway section
    // label 값 설정 -> thread 간 상대적인 order
    // first-come-first-served (선착순) 순서
    int max = 0;
    for(int j = 0; j < label.length; j++) {
      max = Math.max(max, label[j]);
    }
    label[i] = max + 1;

    // 3. waiting section
    // 우선순위가 더 높은 다른 thread들이 없어질 때까지 대기
    // 자기 자신이 아닌 다른 스레드에 대해 ㄱㄱㄱ
    for (int k = 0; k < label.length; k++) {
      if (k != i) { 
        while (flag[k] && (label[k] < label[i] || (label[k] == label[i] && k < i))) {
          Thread.yield(); // CPU를 양보하는 선택적 최적화
        }
      }
    }
  }

  public void unlock() {
    // Critical Section에서 빠져나올 때, flag 해제
    flag[ThreadID.get()] = false;
  }
}
