cr_window_check() {
    startDateTime=$(date -d "$1" +%s)
    endDateTime=$(date -d "$2" +%s)
    
    # Calculate the difference in seconds
    timeDiff=$(( endDateTime - startDateTime ))

    # Check if the difference is greater than or equal to 30 minutes (1800 seconds)
    if [ "$timeDiff" -ge 1800 ]; then
        return 0  # CR is valid
    else
        return 1  # CR is invalid
    fi
}

# Call the function with the start and end datetime
cr_window_check "$startDateTime" "$endDateTime"

if [[ $? -eq 0 ]]; then
    echo "Change Request validation passed"
else
    echo "Change Request validation failed"
fi
