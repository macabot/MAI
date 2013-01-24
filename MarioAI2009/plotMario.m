function plotMario(agent, alpha, gamma, epsilon, training, runs, saveDir)

    %bijvoorbeeld:
    %QLearnAgent_Alpha0.3Gamma0.9Epsilon0.1Training100Runs10.txt
    fileName = strcat(agent, '_',...
        'Alpha',num2str(alpha),...
        'Gamma',num2str(gamma),...
        'Epsilon',num2str(epsilon),...
        'Training',num2str(training),...
        'Runs',num2str(runs) );
    %data = importdata(strcat(fileName, '.txt')); %TODO importdata for matlab
    data = load(strcat(fileName, '.txt'));
    
    averageDistance = data(1,:);
    stdDistance = data(2,:);
    averageReward = data(3,:);
    stdReward = data(4,:);
    %averageCompetitionScore = data(5,:);
    %stdCompetitionScore = data(6,:);
    
    h = figure();

    % plot our reward over the training episodes
    subplot(2,1,1)
    plotGP(1:length(averageReward):training, averageReward, stdReward);
    title(strcat(agent,...
        ', alpha = ',num2str(alpha),...
        ', epsilon = ',num2str(epsilon),...
        ', gamma = ', num2str(gamma) ));
    xlabel('Training episodes');
    ylabel('Reward');
    
    
    % plot the distance over the training episodes
    subplot(2,1,2);
    plotGP(1:length(averageDistance):training, averageDistance, stdDistance);
    xlabel('Training episodes');
    ylabel('Distance travelled');
    
    %{
    % plot the official competition score
    subplot(2,1,3);
    plotGP(1:length(averageCompetitionScore), averageCompetitionScore, stdCompetitionScore);
    xlabel('Training episodes');
    ylabel('Distance travelled');
    title('Competition score')
    %}
    
    % save the plot to pdf
    saveName = strcat(saveDir, fileName, '.png');
    %saveas(h, saveName, 'pdf'); %TODO this is matlab
    print(saveName);
    
end
