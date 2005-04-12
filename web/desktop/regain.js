function showListSelection(listName) {
  var field = document.getElementById(listName + "-entry");
  var list = document.getElementById(listName + "-list");
  
  for (var i = 0; i < list.length; i++) {
    if (list.options[i].selected) {
      field.value = list.options[i].value;
    }
  }
}


function addToList(listName) {
  var field = document.getElementById(listName + "-entry");
  var list = document.getElementById(listName + "-list");
  
  var text = field.value;
  var value = text;
  if (value.length > 0) {
    list.options[list.length] = new Option(text, value);
    field.value = "";
  }
}


function removeFromList(listName) {
  var field = document.getElementById(listName + "-entry");
  var list = document.getElementById(listName + "-list");
  
  // Remove marked entries
  var text = field.value;
  for (var i = 0; i < list.length; i++) {
    if (list.options[i].value == text) {
      list.options[i] = null;
    }
  }
}


function prepareEditListsForSubmit() {
  var forms = document.forms;
  for (var formIdx = 0; formIdx < forms.length; formIdx++) {
    var elements = forms[formIdx].elements;
    for (var elemIdx = 0; elemIdx < elements.length; elemIdx++) {
      var elem = elements[elemIdx];

      if ((elem.type == "select-multiple")
        && (elem.id == (elem.name + "-list")))
      {
        // This is the list of a edit list
        // -> Select all items so they are all sumbitted
        for (var i = 0; i < elem.length; i++) {
          elem.options[i].selected = true;
        }
      }
    }
  }

  return true;
}
