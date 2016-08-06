package com.nexr.newsfeed.client;

import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.entity.User;
import jline.ConsoleReader;
import jline.History;
import jline.SimpleCompletor;

import java.io.File;
import java.util.List;

public class NewsfeedCLI {

    private NewsfeedWebClient client;

    public NewsfeedCLI(String baseUrl) {
        client = new NewsfeedWebClient(baseUrl);
        System.out.println("NewsfeedCLI baseUrl : " + baseUrl);
    }

    public void execute() {

    }

    private static enum COMMAND1 {
        users {
            String help() {
                return "users [<userId>] : returns user info\n" +
                        "users create <email> <name> \n";
            }
        },
        followings {
            String help() {
                return  "followings <userId> : returns list of following \n" +
                        "followings create <userId> <followingId> : <userId> follow <followingId> \n" ;
            }
        },
        activities {
            String help() {
                return "activities create <userId> <message> : <userId> post <message> \n";
            }
        },
        feeds {
            String help() {
                return "feeds [<userId>] [OPTIONS] : returns feeds \n" +
                        "options are: \n" +
                        "-basetime <basetime> \t bastime in milliseconds" +
                        "-maxResult <maxResult> \t maxResult in integer" +
                        "-asc <asc> \t asc in boolean";
            }
        },
        use {
            String help() {
                return "use <userIndex> : set context userId";
            }
        },
        help {
            String help() {
                return "usage: [COMMAND] [SUBCOMMAND] [OPTIONS] \n" +
                        "COMMAND are: \n" +
                        "users : users [get | create] \n" +
                        "followings : followings [get | create] \n" +
                        "activities : activities [create] \n" +
                        "feeds : [get] [OPTIONS]";
            }
        };
        abstract String help();
    }

    private static enum COMMAND {
        submit { String help() { return "submit <app-path> : submit application"; } },
        start { String help() { return "start <job-id> : start submitted job and poll it"; } },
        run { String help() { return "run <app-path> : submit + start application"; } },
        rerun { String help() { return "rerun <job-id> : run the job again"; } },
        kill { String help() { return "kill <job-id> : kill the job"; } },
        killall { String help() { return "killall : kill all the jobs in non-terminal status"; } },
        suspend { String help() { return "suspend <job-id|action-id|action-name(with context)> : suspend the job or action"; } },
        resume { String help() { return "resume <job-id|action-id|action-name(with context)> : resume the job or action"; } },
        update { String help() { return "update <action-id|action-name(with context)> <attr-name=attr-value(,attr-name=attr-value)*> : update action spec. supported only for hive/el/decision action"; } },
        status { String help() { return "status <job-id> : displays status of the job"; } },
        poll { String help() { return "poll <job-id> : poll status of the job. ends when it goes to terminal status"; } },
        cancel { String help() { return "cancel <job-id> : cancels polling the job"; } },
        log { String help() { return "log <job-id|action-id|action-name(with context)> : get logs for the job/action"; } },
        data { String help() { return "data <action-id|action-name(with context)> : retrieves end data for the action"; } },
        xml { String help() { return "xml <job-id|action-id|action-name(with context)> : retrieves definition for the action"; } },
        jobs { String help() { return "jobs [-s start] [-l length] [-c] [-p] [-status status]: retrieves job list"; } },
        actions { String help() { return "actions [-s start] [-l length] [-c] [-p] [-status status]: retrieves action list"; } },
        use { String help() { return "use <job index> : set context job id"; } },
        failed { String help() { return "failed <job-id|action-id|action-name(with context)> : retrieves log URL for failed actions (only for monitored)"; } },
        context { String help() { return "context <job-id> : set context job id"; } },
        reset { String help() { return "reset : remove context job id"; } },
        version { String help() { return "version : shows current version of oozie"; } },
        queue { String help() { return "queue : dump executor queue"; } },
        def { String help() { return "def <job-id|action-id> : shows defintion of the job or action"; } },
        url { String help() { return "url : shows url of oozie server"; } },
        servers { String help() { return "list available Oozie servers"; }},
        example { String help() { return "example <app-path> > : create example on <app-path> (ex> example " +
                "hdfs://nn.nexr.com:8020/user/oozie/shell )"; }},
        quit { String help() { return "quit : quit the shell"; } };
        abstract String help();
    }

    private User currentUser = null;
    private List<User> users = null;

    private boolean executeCommand(String line) {
        //System.out.println("executeCommand : line : " + line);
        String[] commands = line.split("(\\s*,\\s*)|(\\s+)");
        for (int i =0; i<commands.length; i++) {
            System.out.println("command " + i + " : [" + commands[i] + "]");
        }
        String command = commands[0];
        String subcommand = (commands.length > 1) ? commands[1] : null;
//        COMMAND1 command1 = COMMAND1.valueOf(command);
//        if (subcommand == null) {
//            System.out.println(command1.help());
//            return true;
//        }
        System.out.println("COMMAND1.users.name() : [" + COMMAND1.users.name() +"]");
        try {
            if (command.trim().equals(COMMAND1.users.name())) {
                System.out.println("-----------users");
                if (subcommand == null) {

                }
                if (subcommand.equals("create")) {
                    try {
                        String email = commands[2];
                        String name = commands[3];
                        User user = client.createUser(email, name);
                        currentUser = user;
                        System.out.println("succeed!!");
                    }catch (Exception e) {
                        System.out.println("fail, " + e.getMessage());
                    }
                } else { // get
                    System.out.println("-----------users get ");
                    if (subcommand == null) {  // get all
                        System.out.println("-----------users get all");
                        List<User> users = client.getUsers();
                        for (int i=0; i<users.size(); i++) {
                            System.out.println(String.format("user[%s] s%", new Object[]{String.valueOf(i), users.toString()}));
                        }
                        this.users = users;
                    } else {
                        System.out.println("-----------users get inde");
                        try {
                            User user = client.getUser(Integer.parseInt(subcommand));
                            System.out.println(user.toString());
                        } catch (Exception e) {
                            System.out.println(String.format("Fail to get user [%s]", new Object[]{subcommand}));
                        }
                    }
                }
            } else if (command.equals(COMMAND1.followings.name())) {
                if (currentUser == null) {
                    System.out.println("No current user. Execute 'users get' and then 'use <index>'");
                    return true;
                }
                if (subcommand.equals("create")) {
                    try {
                        long userId = currentUser.getId();
                        long following = Long.valueOf(commands[2]);
                        Friend friend = client.follow(userId, following);
                        System.out.println("followings : " + friend.toString());
                    }catch (Exception e) {
                        System.out.println(String.format("Fail to follow : %s", new Object[]{e.getMessage()}));
                    }
                } else { // get
                    try {
                        List<Long> followings = client.getFollowing(currentUser.getId());
                        System.out.println(followings);
                    } catch (Exception e) {
                        System.out.println(String.format("Fail to get following of [%s] : [%s]",
                                new Object[]{currentUser.getId(), e.getMessage()}));
                    }
                }

            } else if (command.equals(COMMAND1.activities.name())) {
                if (currentUser == null) {
                    System.out.println("No current user. Execute 'users get' and then 'use <index>'");
                    return true;
                }
                if (subcommand.equals("create")) {
                    try {
                        client.postMessage(currentUser.getId(), commands[2]);
                        System.out.println("posting succeed!!");
                    } catch (Exception e) {
                        System.out.println(String.format("Fail to create activity : %s", new Object[]{e.getMessage()}));
                    }
                }
            } else if (command.equals(COMMAND1.feeds.name())) {
                if (currentUser == null) {
                    System.out.println("No current user. Execute 'users get' and then 'use <index>'");
                    return true;
                }
                // get


            } else if (command.equals(COMMAND1.use.name())) {
                System.out.println("use userIndex " + subcommand);
                try {
                    User user = users.get(Integer.parseInt(subcommand));
                    currentUser = user;
                } catch (Exception e) {
                    System.out.println("No users. Execute 'users get'");
                }
            } else {
                System.out.println(COMMAND1.help.help());
            }


        } catch (NewsfeedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public void execute(String appPath) throws Exception {

        ConsoleReader reader = new ConsoleReader();
        reader.setBellEnabled(false);

        try {
            File userHome = new File(System.getProperty("user.home"));
            if (userHome.exists()) {
                reader.setHistory(new History(new File(userHome, ".newsfeedhistory")));
            }
        } catch (Exception e) {
            System.err.println("WARNING: Encountered an error while trying to initialize Newsfeed's " +
                    "history file.  History will not be available during this session.");
            System.err.println(e.getMessage());
        }

        SimpleCompletor completor = new SimpleCompletor(new String[0]);
        for (COMMAND1 command : COMMAND1.values()) {
            completor.addCandidateString(command.name());
        }

        reader.addCompletor(completor);

        if (appPath != null) {
            executeCommand(appPath);
        }

        String line;
        String prompt = currentUser == null ? "" : String.valueOf(currentUser.getId());
        while ((line = reader.readLine("NF:" +prompt + "> ")) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (!executeCommand(line)) {
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String baseUrl = (args.length > 0) ? args[0] : "http://localhost:19191";
        NewsfeedCLI newsfeedCLI = new NewsfeedCLI(baseUrl);
        newsfeedCLI.execute("a");
    }

}
