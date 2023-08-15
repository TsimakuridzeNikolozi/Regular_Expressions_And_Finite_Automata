import java.util.*;

public class build {
    public static class Transition implements Comparable<Transition>{
        private Character byWhat;
        private Integer toWhichState;

        public Transition(Character byWhat, Integer toWhichState) {
            this.byWhat = byWhat;
            this.toWhichState = toWhichState;
        }

        public Character getByWhat() {return byWhat;}
        public Integer getToWhichState() {return toWhichState;}

        @Override
        public int compareTo(Transition transition) {
            char byWhat = transition.getByWhat();
            int toWhichState = transition.getToWhichState();
            int comparison1 = Character.compare(this.byWhat, byWhat);
            int comparison2 = Integer.compare(this.toWhichState, toWhichState);

            if (comparison1 == 0) return comparison2;
            return comparison1;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null) return false;
            if (other.getClass() != this.getClass()) return false;
            return this.byWhat == ((Transition) other).getByWhat() &&
                    this.toWhichState == ((Transition) other).getToWhichState();
        }

        @Override
        public String toString() {
            return byWhat + " , " + toWhichState;
        }

        @Override
        public int hashCode() {
            return byWhat.hashCode() + toWhichState.hashCode() +
                    Math.abs(byWhat.hashCode() - toWhichState.hashCode()) * 999;
        }
    }

    public static class NFA {
        int n;
        int a;
        int t;
        ArrayList<Integer> acceptStates;
        HashMap<Integer, ArrayList<Transition>> transitions;

        public NFA() {
            this.setN(1);
            this.setA(1);
            this.setT(0);
            acceptStates = new ArrayList<>();
            acceptStates.add(0);
            transitions = new HashMap<>();
        }

        /**
         * @param c, alphanumeric character
         */
        public NFA(char c) {
            this.setN(2);
            this.setA(1);
            this.setT(1);

            ArrayList<Integer> tempAcceptStates = new ArrayList<>();
            HashMap<Integer, ArrayList<Transition>> tempTransitions = new HashMap<>();

            tempAcceptStates.add(1);
            this.setAcceptStates(tempAcceptStates);

            tempTransitions.put(0, new ArrayList<>(){{add(new Transition(c, 1));}});
            this.setTransitions(tempTransitions);
        }

        /**
         * @param nfa on which the given operator will be applied
         * @param operator, in this case is always '*'(star)
         */
        public NFA(NFA nfa, char operator) {
            // Completes the *(Star) operation by making the start state accepting and making epsilon
            // transitions from every other accepting state to starting one, which means connecting
            // the accepting states to every state which is directly connected to the starting state
            // by the transitions of the starting state

            if (operator == '*') {
                ArrayList<Integer> newAcceptStates = nfa.getAcceptStates();
                HashMap<Integer, ArrayList<Transition>> newTransitions = nfa.getTransitions();

                for(int acceptState: newAcceptStates) {
                    if (acceptState == 0) continue;
                    ArrayList<Transition> transitionsForCurrState = nfa.getTransitions().getOrDefault(acceptState, new ArrayList<>());
                    if (!newTransitions.containsKey(acceptState)) newTransitions.put(acceptState, new ArrayList<>());
                    for(Transition transition: nfa.getTransitions().get(0))
                        if (!transitionsForCurrState.contains(transition))
                            newTransitions.get(acceptState).add(transition);
                }
                if (!newAcceptStates.contains(0)) newAcceptStates.add(0);

                // Setting number of states
                this.setN(nfa.getN());

                // Setting number of accept states and accept states themselves
                this.setAcceptStates(newAcceptStates);
                this.setA(newAcceptStates.size());

                // Setting number of transitions and transitions themselves
                this.setTransitions(newTransitions);
                int sum = 0;
                for(int key: newTransitions.keySet()) {
                    sum += newTransitions.get(key).size();
                }
                this.setT(sum);
            }
        }

        /**
         * Combines the two given NFAs with the given operator.
         * 1) Completes the '&'(concatenation) operation by making the accept states of
         *    the first NFA start states of the second one. This will be accomplished by
         *    making transitions from the first NFAs accept states to the states which are directly
         *    connected to the starting state of the second NFA.
         *
         * 2) Completes the '|'(union) operation by making accept states of both NFAs accept states in
         *    final NFA and making epsilon transition from starting state of first NFA to the starting
         *    state of the second NFA
         * @param nfa1
         * @param nfa2
         * @param operator, in this case is always '&'(intersection) or '|'(union)
         */
        public NFA(NFA nfa1, NFA nfa2, char operator) {
            // Getting new accept states and transitions
            ArrayList<Integer> newAcceptStates = getNewAcceptStates(nfa1, nfa2, operator);
            HashMap<Integer, ArrayList<Transition>> newTransitions = getNewTransitions(nfa1, nfa2, operator);

            // Setting number of states
            this.setN(nfa1.getN() + nfa2.getN() - 1);

            // Setting number of accept states and accept states themselves
            this.setA(newAcceptStates.size());
            this.setAcceptStates(newAcceptStates);

            // Setting number of transitions and transitions themselves
            this.setTransitions(newTransitions);
            int sum = 0;
            for(int key: newTransitions.keySet()) {
                sum += newTransitions.get(key).size();
            }
            this.setT(sum);
        }

        /**
         * Retrieves the new accept states after concatenating or uniting two NFAs.
         *
         * @param nfa1 The first NFA.
         * @param nfa2 The second NFA.
         * @return ArrayList of integers representing the new accept states.
         */
        private ArrayList<Integer> getNewAcceptStates(NFA nfa1, NFA nfa2, char operator) {
            ArrayList<Integer> newAcceptStates = new ArrayList<>();

            // If nfa2's accept states contain state 0, add all accept states from nfa1 to newAcceptStates.
            if (operator == '|' || nfa2.getAcceptStates().contains(0)) {
                nfa1.getAcceptStates().forEach(s -> newAcceptStates.add(s));
            }

            // Add accept states from nfa2 with an offset of nfa1's total states - 1.
            for(int acceptState: nfa2.getAcceptStates()) {
                if (acceptState == 0) continue;
                int newAcceptState = acceptState + nfa1.getN() - 1;
                newAcceptStates.add(newAcceptState);
            }

            return newAcceptStates;
        }

        /**
         * Retrieves the new transitions after concatenating or uniting two NFAs.
         *
         * @param nfa1 The first NFA.
         * @param nfa2 The second NFA.
         * @return HashMap containing the new transitions.
         */
        private HashMap<Integer, ArrayList<Transition>> getNewTransitions(NFA nfa1, NFA nfa2, char operator) {
            HashMap<Integer, ArrayList<Transition>> newTransitions = new HashMap<>();

            newTransitions.putAll(nfa1.getTransitions());
            ArrayList<Transition> transitionsFromSecondNFAStartState = nfa2.getTransitions().getOrDefault(0, new ArrayList<Transition>());

            if (operator == '|' && !nfa1.getAcceptStates().contains(0)){
                for (int i = 0; i < transitionsFromSecondNFAStartState.size(); i++) {
                    Transition transition = transitionsFromSecondNFAStartState.get(i);
                    if (!newTransitions.containsKey(0)) newTransitions.put(0, new ArrayList<>());
                    newTransitions.get(0).add(new Transition(transition.getByWhat(), transition.toWhichState + nfa1.getN() - 1));
                }
            }

            // Iterate over nfa1's accept states.
            for (int state : nfa1.getAcceptStates()) {
                // Iterate over transitions from nfa2's start state.
                for (int i = 0; i < transitionsFromSecondNFAStartState.size(); i++) {
                    Transition transition = transitionsFromSecondNFAStartState.get(i);
                    if (!newTransitions.containsKey(state)) newTransitions.put(state, new ArrayList<>());
                    newTransitions.get(state).add(new Transition(transition.getByWhat(), transition.toWhichState + nfa1.getN() - 1));
                }
            }

            // Adding transitions from the second NFA
            for(int fromWhichState: nfa2.getTransitions().keySet()) {
                if (fromWhichState == 0) continue;
                ArrayList<Transition> transitionsForCurrState = nfa2.getTransitions().get(fromWhichState);
                int newStateNum = fromWhichState + nfa1.getN() - 1;

                if (!newTransitions.containsKey(newStateNum)) newTransitions.put(newStateNum, new ArrayList<>());
                for(Transition t: transitionsForCurrState){
                    newTransitions.get(newStateNum).add(new Transition(t.getByWhat(), t.toWhichState + nfa1.getN() - 1));
                }
            }
            return newTransitions;
        }

        public int getN() {return n;}
        public int getA() {return a;}
        public int getT() {return t;}
        public ArrayList<Integer> getAcceptStates() {return acceptStates;}
        public HashMap<Integer, ArrayList<Transition>> getTransitions() {return transitions;}


        public void setN(int n) {this.n = n;}
        public void setA(int a) {this.a = a;}
        public void setT(int t) {this.t = t;}
        public void setAcceptStates(ArrayList<Integer> acceptStates){this.acceptStates = acceptStates;}
        public void setTransitions(HashMap<Integer, ArrayList<Transition>> transitions) {this.transitions = transitions;}

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            // General Info
            sb.append(this.n + " " + this.a + " " + this.t + "\n");

            // Accept States
            for(int i = 0; i < acceptStates.size() - 1; i++) {
                int state = acceptStates.get(i);
                sb.append(state + " ");
            }
            sb.append(acceptStates.get(acceptStates.size() - 1) + "\n");

            // Transitions
            for(int i = 0; i < n; i++) {
                if (!transitions.containsKey(i)) {
                    sb.append("0\n");
                    continue;
                }

                ArrayList<Transition> transitionsForCurrState = transitions.get(i);
                sb.append(transitionsForCurrState.size() + " ");
                for(int j = 0; j < transitionsForCurrState.size(); j++) {
                    Transition currTransition = transitionsForCurrState.get(j);
                    sb.append(currTransition.getByWhat() + " " + currTransition.getToWhichState());

                    if (j == transitionsForCurrState.size() - 1) continue;
                    sb.append(" ");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    // Operators
    private static final HashSet<Character> operators =
            new HashSet<>(){{add('*'); add('&'); add('|'); add('(');}};

    // Priorities for the operators
    private static final Map<Character, Integer> operatorPriorities =
            new HashMap<>() {{put('*', 1); put('&', 2); put('|', 3); put('(', 4);}};

    /**
     * Converts regular expression to a Reverse Polish Notation
     * @param regularExpression
     * @return processed regular expression
     */
    public static String convertToRPN(String regularExpression) {
        regularExpression = addUnionOperators(regularExpression);
        StringBuilder convertedRegularExpression = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < regularExpression.length(); i++) {
            char currChar = regularExpression.charAt(i);
            if (currChar == '(') {
                stack.push(currChar);
            } else if (currChar == ')') {
                if (stack.isEmpty()) continue;

                StringBuilder temp = new StringBuilder();
                while(stack.peek() != '(') {
                    temp.append(stack.pop());
                }
                stack.pop();
                // '_' will mean empty
                convertedRegularExpression.append(temp.length() == 0 && regularExpression.charAt(i - 1) == '(' ? "_" : temp);
            } else if (operators.contains(currChar)) {
                if (currChar == '*') {
                    convertedRegularExpression.append(currChar);
                    continue;
                }
                while(!stack.isEmpty()) {
                    char operator = stack.peek();
                    if (operatorPriorities.get(operator) <= operatorPriorities.get(currChar))
                        convertedRegularExpression.append(stack.pop());
                    else break;
                }
                stack.push(currChar);
            } else {
                convertedRegularExpression.append(currChar);
            }
        }

        while (!stack.isEmpty()) {
            convertedRegularExpression.append(stack.pop());
        }
        System.out.println(convertedRegularExpression);
        return convertedRegularExpression.toString();
    }

    /**
     *
     * @param regularExpression
     * @return regularExpression with the added union operators
     */
    private static String addUnionOperators(String regularExpression) {
        StringBuilder newRegularExpression = new StringBuilder();

        for (int i = 0; i < regularExpression.length(); i++) {
            if (i == regularExpression.length() - 1) {
                newRegularExpression.append(regularExpression.charAt(i));
                break;
            }
            char currChar = regularExpression.charAt(i);
            char nextChar = regularExpression.charAt(i + 1);

            if ((Character.isLetterOrDigit(currChar) || currChar == ')' || currChar == '*') && nextChar == '(') {
                newRegularExpression.append(currChar + "&");
            } else if (Character.isLetterOrDigit(nextChar) && (currChar == '*' || currChar == ')' || Character.isLetterOrDigit(currChar))) {
                newRegularExpression.append(currChar + "&");
            } else {
                newRegularExpression.append(currChar);
            }
        }
        System.out.println(newRegularExpression);
        return newRegularExpression.toString();
    }

    /**
     * Controls the building process of the NFA
     * @param regularExpression
     */
    public static NFA buildNFA(String regularExpression) {
        String processedRegularExpression = convertToRPN(regularExpression);
        NFA nfa = new NFA();
        Stack<NFA> stack = new Stack<>();

        for(char currChar: processedRegularExpression.toCharArray()) {
            if (currChar == '_') // Empty
                stack.push(new NFA());
            else if (!operators.contains(currChar))
                stack.push(new NFA(currChar));
            else if (currChar == '&' || currChar == '|') {
                NFA nfa1 = stack.pop(), nfa2 = stack.pop();
                stack.push(new NFA(nfa1, nfa2, currChar));
            }
            else if (currChar == '*') {
                NFA nfa1 = stack.pop();
                stack.push(new NFA(nfa1, currChar));
            }
        }

        if (!stack.isEmpty()) nfa = stack.pop();
        return nfa;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String regularExpression = sc.nextLine();
        NFA nfa = buildNFA(regularExpression);
        System.out.println(nfa.toString());
    }
}