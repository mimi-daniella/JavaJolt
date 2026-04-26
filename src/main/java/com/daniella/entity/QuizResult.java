package com.daniella.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private int score;
    private int totalQuestions;
    private double percentageScore;
    private boolean passed;
    private int xpEarned;
    private String categoryFocus;
    private String difficultyFocus;
    private LocalDateTime  completedAt;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable= false)
    private User user;
    
    
    
    //getters & setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTotalQuestions() {
		return totalQuestions;
	}

	public void setTotalQuestions(int totalQuestions) {
		this.totalQuestions = totalQuestions;
	}

	public double getPercentageScore() {
		return percentageScore;
	}

	public void setPercentageScore(double percentageScore) {
		this.percentageScore = percentageScore;
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public int getXpEarned() {
		return xpEarned;
	}

	public void setXpEarned(int xpEarned) {
		this.xpEarned = xpEarned;
	}

	public String getCategoryFocus() {
		return categoryFocus;
	}

	public void setCategoryFocus(String categoryFocus) {
		this.categoryFocus = categoryFocus;
	}

	public String getDifficultyFocus() {
		return difficultyFocus;
	}

	public void setDifficultyFocus(String difficultyFocus) {
		this.difficultyFocus = difficultyFocus;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
