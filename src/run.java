import java.util.*;
public class run {
    public static class Transition implements Comparable<Transition> {
        private Integer fromWhichState;
        private Character byWhat;

        public Transition(Integer fromWhichState, Character byWhat) {
            this.fromWhichState = fromWhichState;
            this.byWhat = byWhat;
        }


        public Integer getFromWhichState() {return fromWhichState;}
        public Character getByWhat() {return byWhat;}


        @Override
        public int compareTo(Transition transition) {
            int fromWhichState = transition.getFromWhichState();
            char byWhat = transition.getByWhat();
            int comparison1 = Integer.compare(this.fromWhichState, fromWhichState);
            int comparison2 = Character.compare(this.byWhat, byWhat);

            if (comparison1 == 0) return comparison2;
            return comparison1;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null) return false;
            if (other.getClass() != this.getClass()) return false;
            return this.fromWhichState == ((Transition) other).getFromWhichState() &&
                    this.byWhat == ((Transition) other).getByWhat();
        }

        @Override
        public String toString() {
           return fromWhichState + " , " + byWhat;
        }

        @Override
        public int hashCode() {
            return fromWhichState.hashCode() + byWhat.hashCode() +
                    Math.abs(fromWhichState.hashCode() - byWhat.hashCode()) * 999;
        }
    }

    private static String runSimulation(String input, int n, int a, int t, HashSet<Integer> acceptStates,
                                        HashMap<Transition, HashSet<Integer>> transitions) {
        String answer = "";
        ArrayList<Integer> currentStates = new ArrayList<>(){{add(0);}};

        for(int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            ArrayList<Integer> temp = new ArrayList<>(); // Will hold next States
            for(int currentState: currentStates) {
                Transition possibleTransition = new Transition(currentState, currentChar);
                if (!transitions.containsKey(possibleTransition)) continue;

                for(int toWhichState: transitions.get(possibleTransition)) {
                    temp.add(toWhichState);
                    if (acceptStates.contains(toWhichState)) answer = answer.length() == i ? answer + "Y" : answer;
                }
            }

            answer = answer.length() == i ? answer + "N" : answer;
            currentStates = temp;
        }

        return answer;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String input = scanner.nextLine(); // String which will be tested
        int numStates = scanner.nextInt(), numAcceptStates = scanner.nextInt(), numTransitions = scanner.nextInt();
        HashSet<Integer> acceptStates = new HashSet<>();
        HashMap<Transition, HashSet<Integer>> transitions = new HashMap<>();

        // Reading accept states
        for(int i = 0; i < numAcceptStates; i++) {
            int acceptState = scanner.nextInt();
            acceptStates.add(acceptState);
        }

        // Reading transitions
        for(int fromWhichState = 0; fromWhichState < numStates; fromWhichState++) {
            int numCurrTransitions = scanner.nextInt();

            for(int j = 0; j < numCurrTransitions; j++) {
                char byWhat = scanner.next().charAt(0);
                int toWhichState = scanner.nextInt();
                Transition transition = new Transition(fromWhichState, byWhat);

                transitions.computeIfAbsent(transition, k -> new HashSet<>()).add(toWhichState);
            }
        }

        String answer = runSimulation(input, numStates, numAcceptStates, numTransitions, acceptStates, transitions);
        System.out.println(answer);
    }
}
