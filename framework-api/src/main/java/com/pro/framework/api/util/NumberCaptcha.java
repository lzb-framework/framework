package com.pro.framework.api.util;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.RandomUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

public class NumberCaptcha extends AbstractCaptcha {

    /**
     * 构造
     *
     * @param width 图片宽
     * @param height 图片高
     */
    public NumberCaptcha(int width, int height) {
        this(width, height, 4);
    }

    /**
     * 构造
     *
     * @param width 图片宽
     * @param height 图片高
     * @param codeCount 字符个数
     */
    public NumberCaptcha(int width, int height, int codeCount) {
        this(width, height, codeCount, 0);
    }
    /**
     * 构造
     *
     * @param width 图片宽
     * @param height 图片高
     * @param codeCount 字符个数
     * @param interfereCount 验证码干扰元素个数
     */
    public NumberCaptcha(int width, int height, int codeCount, int interfereCount) {
        super(width, height, new RandomGenerator(RandomUtil.BASE_NUMBER, codeCount), interfereCount);
    }

    @Override
    public Image createImage(String code) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ImgUtil.createGraphics(image, Color.WHITE);

        // 画字符串
        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        final FontMetrics metrics = g.getFontMetrics();
        final int minY = metrics.getAscent() - metrics.getLeading() - metrics.getDescent();
        int len = code.length();
        int charWidth = width / len;
        for (int i = 0; i < len; i++) {
            // 指定透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            g.setColor(ImgUtil.randomColor());
            g.drawString(String.valueOf(code.charAt(i)), i * charWidth, RandomUtil.randomInt(minY, this.height));
        }

        return image;
    }
}
