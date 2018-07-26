/*
Navicat MySQL Data Transfer

Source Server         : 172.27.107.16
Source Server Version : 50173
Source Host           : 172.27.107.16:3306
Source Database       : filestorage

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2018-07-26 15:29:25
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for file_info
-- ----------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `md5` varchar(64) NOT NULL COMMENT '文件MD5值',
  `size` bigint(20) DEFAULT '0' COMMENT '文件大小',
  `gmt_create` datetime DEFAULT NULL,
  `status` varchar(32) DEFAULT '0',
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=70 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for file_info_ext
-- ----------------------------
DROP TABLE IF EXISTS `file_info_ext`;
CREATE TABLE `file_info_ext` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `base_fid` int(11) NOT NULL,
  `original_file_name` text NOT NULL,
  `suffix` varchar(32) DEFAULT NULL,
  `file_type` varchar(32) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=85 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for folder
-- ----------------------------
DROP TABLE IF EXISTS `folder`;
CREATE TABLE `folder` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `pid` int(11) DEFAULT '0' COMMENT '此文件夹的父文件夹id',
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) DEFAULT NULL COMMENT '文件夹分类',
  `is_empty` tinyint(1) DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for folder_file
-- ----------------------------
DROP TABLE IF EXISTS `folder_file`;
CREATE TABLE `folder_file` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `folder_id` int(11) NOT NULL,
  `file_id` int(11) NOT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `file_id` (`file_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `bucket` varchar(255) DEFAULT NULL COMMENT '来自哪里的用户',
  `disable` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- View structure for v_file_detail_info
-- ----------------------------
DROP VIEW IF EXISTS `v_file_detail_info`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `v_file_detail_info` AS select `file_info`.`file_name` AS `file_name`,`file_info`.`md5` AS `md5`,`file_info`.`size` AS `size`,`file_info_ext`.`id` AS `id`,`file_info_ext`.`base_fid` AS `base_fid`,`file_info_ext`.`original_file_name` AS `original_file_name`,`file_info_ext`.`suffix` AS `suffix`,`file_info_ext`.`file_type` AS `file_type`,`file_info_ext`.`gmt_create` AS `gmt_create`,`file_info_ext`.`gmt_modify` AS `gmt_modify`,`file_info_ext`.`gmt_delete` AS `gmt_delete`,`file_info`.`status` AS `status` from (`file_info` join `file_info_ext`) where (`file_info`.`id` = `file_info_ext`.`base_fid`) ;
