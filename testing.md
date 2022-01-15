
#Before
- Start without internet connection


# Menu Bar -> Configure

- configure in menu bar opens preference page
- login with wrong username or password shows error dialog on apply
- login with correct username or password does not show a dialog

# AllAssignmentsview 

- try selecting assignment without selected Course in Reviewer settings -> shows error dialog
- try loading assignments without selected Course in Reviewer Settings -> shows error dialog

- try download submission without tutor rights
- if there is no internet connection there should be a empty combobox for the assignment and
    the list should bve empty too
    - also if the download all button is clicked no download should start
- if a assignment is selected and there are submissions group in the stumgmt group
    - the should be displayed in the list
    - if you click on one of them and the had a submission you should see in the -> Review view
     problems and the review data
    - you should be able to right click and download a single one

- If you download one or all and there is internet
    - there should come a result dialog after the download
    - there should be a progressbar in bottom right
    - there should be a job in the eclipse job view
    - the downloaded project should be displayed in the Navigator
    - there should be a workset created that can be selected to group the downloaded submissions

   
# Review view
   - If no group or project in the navigator is selected or the internet connection is not                          
     available
       - there should be not problems in the problem list and no review data
       
   - If a group in the -> AllAssignmentsview is selected or a project in the Navigator 
     selected there should be Reviewdata and when problems/ warnings are in the submissions
     it should be displayed in the problems list
     
     
     

# Navigator
   - If there is not internet connection or the selected project is not a
     submission there should be no Reviewdata in the -> Review view.
     -> there should no lag and no more Errordialogs than one.
     
   - If there is connection and the selected project is a submission the Reviewdata should be
     displayed in the -> Review view
     
     -
     





