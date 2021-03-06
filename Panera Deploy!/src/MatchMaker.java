/**
 * Created by Mark on 4/29/16.
 */


import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.ArrayDeque;

public class MatchMaker {

    final static int OPENING_TIME = 500;
    final static int CLOSING_TIME = 2300;
    final static int LUNCH_SHIFT = 1030;
    final static int DINNER_SHIFT = 1300;

    private EmployeeSkillMap skills;
    private ArrayDeque<Position> unassignedPositions;

    private boolean breakTheRules;      //TODO Don't touch this!!! - Mark

    public MatchMaker(EmployeeSkillMap skills, ArrayDeque<Position> positions){
        this.skills = skills;
        this.unassignedPositions = positions;
        this.breakTheRules = false;
    }

    public void setPostionQueue(ArrayDeque<Position> newPositionQueue){
        this.unassignedPositions = newPositionQueue;
    }
  
    public ArrayList<Position> frontMatch(PriorityQueue<Employee> employeeQ, boolean driveThru){

        ArrayList<Position> assignedPositions = new ArrayList(employeeQ.size());    //holds the final list of Positions
        ArrayList<Position> temp = new ArrayList();                                 //holds a temporary list of positions to pick from first

        ArrayList<Employee> unassignedEmployees = new ArrayList();                  //holds a temp list of unassigned employees

        breakTheRules = false;

        //handle special positions
        // TODO: finish this up, needs to go thru
        /*
        make sure the lunch manager is the second manager coming in
        unless there is none that come in before lunch then make it the opening manager - Cameron
         */
        /*
        if (!driveThru){

            //remove all special cases from unassigned position queue
            while(unassignedPositions.peek().getOrderOfImport() == 0){
                temp.add(unassignedPositions.remove());
                if (Driver.test)
                    System.out.println("Removing SPECIAL positions");
            }
        }
*/
        if(Driver.test)
            System.out.println("Beginning matching loop...");

        //begin the matching loop
        //this loop begins at a opening time of the store and increments every fifteen minutes until the store closes
        //or no more employees with
        for(Time currentTime = new Time(OPENING_TIME); currentTime.isBefore(CLOSING_TIME) ||
                employeeQ.isEmpty(); currentTime.addTime(15)){


            //check if it is the start of a new shift
            if(currentTime.equals(LUNCH_SHIFT)) {
                //empty out temp list of positions
                int index = 0;
                while(temp.get(index).getOrderOfImport() < 2 && !temp.isEmpty() && index < temp.size()) {
                    if (temp.get(index).getOrderOfImport() == 0)
                        index++;
                    temp.remove(index);
                    if (temp.isEmpty())
                        break;
                }
                while (unassignedPositions.peek().getOrderOfImport() == 1) {
                    unassignedPositions.remove();

                    if (Driver.test && currentTime.equals(LUNCH_SHIFT))
                        System.out.println("Flushing unassigned breakfast positions...");
                }
            }

            if (currentTime.equals(DINNER_SHIFT)) {
                if (Driver.test)
                    System.out.println("Flushing unassigned lunch positions");
                int index = 0;
                while(temp.get(index).getOrderOfImport() < 3 && !temp.isEmpty() && index < temp.size()){
                    if(temp.get(index).getOrderOfImport() == 0)
                        index++;
                    temp.remove(index);
                    if(temp.isEmpty())
                        break;
                }
                while (unassignedPositions.peek().getOrderOfImport() == 2)
                    unassignedPositions.remove();
            }



        	if(Driver.test)
        		System.out.println(currentTime);
            //move any employees just now coming in to the list of employees needing a position
            while(employeeQ.peek().getStartTime().equals(currentTime)) {
                unassignedEmployees.add(employeeQ.remove());
                if(!unassignedPositions.isEmpty())
                    temp.add(unassignedPositions.remove());
            }

            //where the magic happens
            while(!unassignedEmployees.isEmpty()){

                //if there is only one employee
               if (unassignedEmployees.size() == 1){
                   boolean found = false;
                    for(int i = 0; i < temp.size(); i++) {
                        if (skills.checkSkill(unassignedEmployees.get(0).getName(), temp.get(i).getSkill())) {
                            temp.get(i).assignEmployee(unassignedEmployees.remove(0));
                            assignedPositions.add(temp.remove(i));
                            found = true;
                            break;
                        }
                    }
                        if (!found){
                            if(Driver.test)
                                System.out.println("MATCH NOT FOUND, looking thru list of unassigned positions");
                            while(!skills.checkSkill(unassignedEmployees.get(0).getName(),
                                    unassignedPositions.peek().getSkill())){
                                temp.add(unassignedPositions.remove());
                                if(Driver.test)
                                    System.out.println("Match not yet found...");
                            }
                            if(Driver.test)
                                System.out.println("FOUND MATCH for Employee: " + unassignedEmployees.get(0).getName());
                            //TODO Check to see if the positions are still in the same shift as the employee
                            //assign employee to current position from unAssignedPositions
                            unassignedPositions.peek().assignEmployee(unassignedEmployees.remove(0));   //assign employee to position
                            assignedPositions.add(unassignedPositions.poll());                          //move position to list of assigned positions
                            break;
                        }

                }
            	System.out.println("Made it to the Magic");
                //first assign any employees with only 1 skill
                for (int i = 0; i < unassignedEmployees.size() && unassignedEmployees.size() > 1; i++){
                    //find any employee with only one skill
                    if(skills.getNumberOfSkills(unassignedEmployees.get(i).getName()) == 1){
                    	System.out.println("found an employee with only one skill");
                        //search temp list of positions
                        for(int j = 0; j < temp.size() && unassignedEmployees.size() > 1; j++) {
                            //if there is a position that matches the current employee's skill
                            if (skills.checkSkill(unassignedEmployees.get(i).getName(), temp.get(j).getSkill())) {
                                if (Driver.test)
                                    System.out.println("FOUND MATCH for Employee: " + unassignedEmployees.get(i).getName());

                                temp.get(j).assignEmployee(unassignedEmployees.remove(i));      //assign employee to position
                                assignedPositions.add(temp.remove(j));                          //move position to final list
                            }
                            //if employee does not match any of the temp list draw from unassigned positons till a match is found
                        }
                                if(Driver.test)
                                    System.out.println("MATCH NOT FOUND, looking thru list of unassigned positions");
                                while(!skills.checkSkill(unassignedEmployees.get(i).getName(),
                                        unassignedPositions.peek().getSkill())){
                                    temp.add(unassignedPositions.remove());
                                    if(Driver.test)
                                        System.out.println("Match not yet found...");
                                }
                                if(Driver.test)
                                    System.out.println("FOUND MATCH for Employee: " + unassignedEmployees.get(i).getName());
                                //TODO Check to see if the positions are still in the same shift as the employee
                                //assign employee to current position from unAssignedPositions
                                unassignedPositions.peek().assignEmployee(unassignedEmployees.remove(i));   //assign employee to position
                                assignedPositions.add(unassignedPositions.poll());                          //move position to list of assigned positions


                    }
                }if(Driver.test)
                    System.out.println("Did not find any employees with 1 skill\nMoving to step 2.");
                //repeats steps 2 and 3 until list of unassigned employees is empty
                while (!unassignedEmployees.isEmpty() && unassignedEmployees.size() > 1) {

                    if(Driver.test){
                        System.out.println("Starting step 2.");
                    }
                    //second assign an employee to any positions that have only one employee to match
                    for (int i = 0; i < unassignedEmployees.size() && unassignedEmployees.size() > 1; i++) {
                        //loop thru as many temp positions as there are employees, and count how many employees can fill that skill
                        //if only 1 employee can, assign them
                        //TODO is there a way to keep track of how many employees can fill a position without iterating over every position and employee?
                        int count = 0;
                        for (int j = 0; j < unassignedEmployees.size() && unassignedEmployees.size() > 1; j++) {
                            int e = -1;
                            if (Driver.test)
                                System.out.println("Potential ArrayOutOfBounds exception here...");
                            if (skills.checkSkill(unassignedEmployees.get(j).getName(), temp.get(i).getSkill())) {
                                count++;
                                e = j;
                                //if there are 2 employees that can fill a position, go ahead and skip to the next position
                                if(count > 1){
                                    continue;
                                }
                            }
                            //if only one employee matches
                            if (count == 1) {
                                if(Driver.test) {
                                    System.out.println("FOUND MATCH for Employee: " + unassignedEmployees.get(e).getName());
                                    System.out.println("POSITION: " + temp.get(i).getSkill());
                                }
                                temp.get(i).assignEmployee(unassignedEmployees.remove(e));     //assign employee at e
                                assignedPositions.add(temp.remove(i));                         //move position to list of assignedPositions
                                if (unassignedEmployees.size() == 1)
                                    break;
                            }

                        }
                    }

                    //third assign the next employee in the list to the first position then can fill
                    //TODO:want to include TRYING TO MAKE THE BEST MATCH then this is where it would be
                    if(Driver.test)
                        System.out.println("Starting Step 3");
                    for(int i = 0; i < unassignedEmployees.size() && unassignedEmployees.size() > 1; i++){
                        if(skills.checkSkill(unassignedEmployees.get(0).getName(), temp.get(i).getSkill())){
                            if(Driver.test)
                                System.out.println("FOUND MATCH for Employee: " + unassignedEmployees.get(0).getName());
                            temp.get(i).assignEmployee(unassignedEmployees.remove(0));
                            assignedPositions.add(temp.remove(i));
                            break;
                        }
                        else{
                            temp.add(unassignedPositions.poll());
                        }
                    }
                }
            }
        }
        if(Driver.test){
            System.out.println("Finished matching.");
        }
        return assignedPositions;
    }
    public ArrayList<Position> driveMatch(PriorityQueue<Employee> employeeQ, boolean driveThru){
    	if(employeeQ == null)
    		System.out.println("No Employee's Found");
    	ArrayList<Position> assignedPositions = new ArrayList(employeeQ.size());    //holds the final list of Positions
        ArrayList<Position> dBreakfast = new ArrayList();
        ArrayList<Position> dLunch = new ArrayList();
        ArrayList<Position> dDinner = new ArrayList();
        PriorityQueue<Employee> breakfastE = new PriorityQueue();
        PriorityQueue<Employee> lunchE = new PriorityQueue();
        PriorityQueue<Employee> supperE = new PriorityQueue();
        ArrayList<Employee> unassignedEmployees = new ArrayList();					 //holds a temp list of unassigned employees
        PriorityQueue tempQ = employeeQ;
        
        for(int i = 1; i <= unassignedPositions.size(); i++){
       	 if(i < 5){
       		 dBreakfast.add(unassignedPositions.remove());
       	 }else if(i > 5 && i <= 11){
       		 dLunch.add(unassignedPositions.remove());
       	 }else if(i > 11 && i <= 17){
       		 dDinner.add(unassignedPositions.remove());
       	 }
        }	
        for(int i = 1; i <= employeeQ.size(); i++){
        	if (employeeQ.peek().getStartTime().compareTo(LUNCH_SHIFT) == -1){
        		breakfastE.add(employeeQ.peek());
        		
        	}
        	if (employeeQ.peek().getStartTime().compareTo(DINNER_SHIFT) == -1 && employeeQ.peek().getEndTime().compareTo(LUNCH_SHIFT) == 1){
        		lunchE.add(employeeQ.peek());
        	}
        	if (employeeQ.peek().getStartTime().compareTo(DINNER_SHIFT) == 0 || employeeQ.peek().getStartTime().compareTo(DINNER_SHIFT) == 1){
        		supperE.add(employeeQ.peek());
        	}
        	employeeQ.remove();
        }
        
        
        for(Time currentTime = new Time(OPENING_TIME); currentTime.isBefore(CLOSING_TIME) ||
                employeeQ.isEmpty(); currentTime.addTime(15)){

       	 if(currentTime.compareTo(LUNCH_SHIFT) == -1){
       		 while(employeeQ.peek().getStartTime().equals(currentTime)) {
                    unassignedEmployees.add(breakfastE.remove());
                }
       		 for(int i = 0; i < dBreakfast.size(); i++){
       				 dBreakfast.get(i).assignEmployee(unassignedEmployees.remove(0));
       				 assignedPositions.add(dBreakfast.remove(i));
       		 }
       	 }
       	 if((currentTime.compareTo(LUNCH_SHIFT) == 0 || currentTime.compareTo(LUNCH_SHIFT) == 1) && currentTime.compareTo(DINNER_SHIFT) == -1){
       		 while(lunchE.peek().getStartTime().equals(currentTime)) {
                 unassignedEmployees.add(lunchE.remove());
             }
       		 while(!unassignedEmployees.isEmpty()){
       			 int count = 1;
            	 for (int i = 0; i < dLunch.size(); i++){
            		 if(unassignedEmployees.get(0).equals(dLunch.get(i).getSkill())){
            			 dLunch.get(i).assignEmployee(unassignedEmployees.get(0));
            			 assignedPositions.add(dLunch.remove(i));
            			 break;
            		 }
            		count++;
            	 }
            	 if(count == dLunch.size()){
            		 Employee tempE;
            		 for(int i = 0; i < assignedPositions.size(); i++){
            			 if(unassignedEmployees.get(0).equals(assignedPositions.get(i).getSkill())){
            				 tempE = assignedPositions.get(i).getEmployee();
            				 for(int x = 0; x < dLunch.size(); x++){
            					 if (tempE.equals(dLunch.get(x).getSkill())){
            						 dLunch.get(x).assignEmployee(tempE);
            						 assignedPositions.add(dLunch.remove(x));
            						 assignedPositions.get(i).changeEmployee(unassignedEmployees.get(0));
            					 }
            				 }
            			 }
            		 }
            	 }
             }
            	 
       		 
       	 }
       	 if(currentTime.compareTo(DINNER_SHIFT) == 0 || currentTime.compareTo(DINNER_SHIFT) == 1){
       		 
       	 }
        }
   	
   return assignedPositions;
   }
}
