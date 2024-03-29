## About

This is the old repository for the upcoming website of FalconX Robotics. This project is currently not in development.

[Here](https://falconxrobotics.herokuapp.com/) is the link (inactive).

## Cautions
Remember to never leak Google Sheets authentication files onto git.
If you do, remember to immediately:
- Make the repo private
- Undo the changes
- Refresh the tokens

### How to push to Heroku without leaking Credentials
Run the following commands on git bash.
```
git branch -D product
git branch product master
git checkout product
```
Then, remove `secrets/` in [.gitignore](.gitignore) and run the following.
```
git commit -a -m "Added Credentials for Heroku."
git push heroku product:master -f
```
Lastly, to return back to the master branch:
```
git checkout master
```
