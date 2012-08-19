 ___          ___ ___________     _________    ___       ___
 |  \        /  | |  ________|   / ________|   |  |      |  |
 |   \      /   | |  |          / /            |  |      |  |
 |    \    /    | |  |          \ \            |  |      |  |
 |  |\ \  / /|  | |  |_______    \ \_______    |  |______|  |
 |  | \ \/ / |  | |  ________|    \_______ \   |  _______   |
 |  |  \__/  |  | |  |                    \ \  |  |      |  |
 |  |        |  | |  |                    / /  |  |      |  |
 |  |        |  | |  |_______   _________/ /   |  |      |  |
 |__|        |__| |__________|  |_________/    |__|      |__|
   _________    ___________   __________    __           __   ____________
  / ________|   |  ________|  |  ______ \   | |          | |  |  _________|
 / /            |  |          |  |     \ \  | |          | |  |  |
 \ \            |  |          |  |     / /  \ \          / /  |  |
  \ \_______    |  |_______   |  |____/ /    \ \        / /   |  |________
   \_______ \   |  ________|  |   _  __/      \ \      / /    |  _________|
           \ \  |  |          |  | \ \         \ \    / /     |  |
           / /  |  |          |  |  \ \         \ \  / /      |  |
 _________/ /   |  |_______   |  |   \ \         \ \/ /       |  |________
 |_________/    |__________|  |__|    \_\         \__/        |___________|
 
 v1.1.0

 Copyright © 2012 Dominic Clark (TheSuccessor)
____________________________

  License
_____________________________

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
_____________________________
 
  ReadMe
_____________________________

Main Screen

  "Start" - This starts the server using the current settings. Clients cannot
            connect unless the server is running.

  "Stop" - Stops the server.

  "Configure" - Opens the configuration dialog.

  Clients table

    "IP address" - The IP address of the client.

    "Port number" - The remote port number of the client.

    "Group" - The group the client is currently in. This can be changed by double-
              clicking the cell, typing the name of the new group, then pressing
              enter.

  "Set as server" - This is only available if the server mode is set to "Clients
                    and server". Select a client from the clients list and press
                    this button to set it as the server for its group.

  "Evict" - This will evict the currently selected client from the server. If it
            is set as the server on a "Clients and Server" type server, a dialog
            box will appear to confirm the eviction.

  "Mute" - This prevents the currently selected client from sending messages to the
           server. If the currently selected client is already muted, it will be
           labelled "Unmute" and pressing it will allow the client to send messages
           again. Note that this does not prevent the client receiving messages.

  "Send message" - This button will send the message in the adjacent text box to all
                   clients currently connected to the server, regardless of the
                   group they occupy. It does not just broadcast the text - the full
                   message must be included (e.g. "broadcast "hello world"" or
                   "sensor-update "var" 10").

  Server log - This is the text area under the "Send message" button. The server logs
               events and exceptions here. If an exception occurs, please copy it and
               post it at http://scratch.mit.edu/forums/viewtopic.php?id=102532 so I
               can try and fix it. (An exception generally contains the text
               "Exception" somewhere and has multiple indented lines beginning with
               "at".)

Configuration dialog

  "OK" - Saves any settings changes that have been made, then exits the dialog. Saved
         options are stored in a file named "meshserve.dat", and persist between
         uses. This option is not available whilst the server is running.

  "Cancel" - Exits the dialog without saving setting changes.

  "Server" tab

    "Server IP address" - Sets the IP address on which the server runs. The program
                          automatically detects the available IP addresses to bind
                          to and displays them in the list. This is the IP address
                          that needs to be entered into Scratch in order to connect
                          (except if you are port-forwarding, in which case the IP
                          to connect to is your external IP and this box should be
                          set to whichever IP you port-forwarded to).

    "Enable logging" - If checked, any server events will be shown on the server log.
                       If not, only exceptions thrown by the program will be logged.

    "Clear log" - Pressing this button will clear the server log. It does not wait for
                  OK to be pressed, and is not undoable.

    "Max clients" - Sets the maximum number of clients that can be connected to the
                    server at once.

    This server is of type...

    "Mesh" - Behaves the same as a normal Scratch mesh network. All messages received
             in a group are sent to all members of the same group (unless, of course,
             the sender is muted).

    "Clients and Server" - In this mode, one client from each group can be set as the
                           "server" for that group. All messages sent by the server
                           are sent to all clients in its group. However, when a
                           non-server sends a message, it is only sent to the server
                           of the group. This is useful for stopping other clients
                           listening in on messages only intended for the server, or
                           for stopping broadcasts unnecessarily activating scripts
                           on all clients, for example.
  "Groups" tab

    Groups table

      "Name" - The name of the group. This can be edited by double-clicking the cell,
               typing the new name, then pressing enter. All groups must have
               different names.

      "Max clients" - The maximum number of clients that can occupy this group. This
                      can be edited the same way as before. The default group must
                      have at least the same number as the client cap on the server.

      "Number of clients" - The number of clients currently in this group.

      "Properties" - The properties of this group. These can be changed using the
                     buttons below.

    "Add new" - This adds a new group. There is no limit on the number of groups.

    "Delete" - Deletes the currently selected group. The default group cannot be
               deleted, and there must always be at least one group.

    "Set as default" - Sets the currently selected group as the default. This is the
                       group that clients are automatically put into when they join.
                       The group must be public and have a max clients number at
                       least that of the client cap on the server.

    Group type - Sets the group type to either public or invite. Any unmuted client
                 can join a public group by broadcasting <GroupName to the server.
                 Clients can only join invite groups by having their group set in the
                 clients table on the main screen.