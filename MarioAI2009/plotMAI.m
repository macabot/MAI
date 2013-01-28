function plotMAI()
    close all;

    % initialising values used by q-learning / sarsa
    agent = 'QLearnAgent';
    alpha = 0.3;
    gamma = 0.9;
    epsilon = 0.1;
    %state = '';
    training = 100; % amount of times that training is done
    episodes = 3; % averaging the result over 10 runs
    steps = 1;
    saveDir = '../MAIpngs/';
    
    plotMario(agent, alpha, gamma, epsilon, training, episodes, steps, saveDir);

end 
